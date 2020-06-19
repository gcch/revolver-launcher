/*
 * Copyright (C) 2014-2015 NOIT, School a-apps project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.noit.revolverlauncher;

import java.util.Date;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.amazon.device.iap.*;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.ProductType;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;

public class AmznPurchasingListener implements PurchasingListener {
//  private static final String TAG = "PurchasingListener";

  // getUserData() で取得した情報
  private String currentUserId = null;
  private String currentMarketplace = null;

  // モード
  private boolean rvsProductionMode = false;
  public static final boolean reset = true;  // getPurchaseUpdates を呼び出すときの引数 (true = 毎回読みなおす)

  private SettingActivity activity;
  private ParameterManager parameterManager;

  public AmznPurchasingListener(SettingActivity activity) {
//    Log.d(TAG, "[IAB] AmznPurchasingListener");
    this.activity = activity;
    parameterManager = new ParameterManager(activity.getApplicationContext());
    rvsProductionMode = !PurchasingService.IS_SANDBOX_MODE;
  }

  public static final String SKU_PREMIUM = "revolver_launcher_full_edition_key";

  /**
   * onUserDataResponse --- アカウント情報の取得 (getUserData の Callback)
   */
  @Override
  public void onUserDataResponse(final UserDataResponse response) {
//    Log.d(TAG, "[IAB] onUserDataResponse");
    final UserDataResponse.RequestStatus status = response.getRequestStatus();

    switch(status) {
    case SUCCESSFUL:  // 取得に成功
      currentUserId = response.getUserData().getUserId();
      currentMarketplace = response.getUserData().getMarketplace();
      break;

    case FAILED:
    case NOT_SUPPORTED:
      break;
    }
  }

  /**
   * プロダクト情報の取得
   */
  @Override
  public void onProductDataResponse(final ProductDataResponse response) {
//    Log.d(TAG, "[IAB] onProductDataResponse");
    final ProductDataResponse.RequestStatus status = response.getRequestStatus();
    switch (status) {
    case SUCCESSFUL:  // 成功
//      Log.v(TAG, "[IAB] ProductDataRequestStatus: SUCCESSFUL");
//      for (final String s : response.getUnavailableSkus()) {
//        Log.v(TAG, "[IAB] Unavailable SKU:" + s);
//      }
//      final Map<String, Product> products = response.getProductData();
//      for (final String key : products.keySet()) {
//        Product product = products.get(key);
//        Log.v(TAG, String.format("[IAB] Product: %s\n[IAB] Type: %s\n[IAB] SKU: %s\n[IAB] Price: %s\n[IAB] Description: %s\n", product.getTitle(), product.getProductType(), product.getSku(), product.getPrice(), product.getDescription()));
//      }
      break;

    case FAILED:  // 失敗
    case NOT_SUPPORTED:
//      Log.v(TAG, "[IAB] ProductDataRequestStatus: FAILED");
      break;
    }
  }

  /**
   * onPurchaseUpdatesResponse --- 過去の購入状況の確認
   */
  @Override
  public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse response) {
//    Log.d(TAG, "[IAB] onPurchaseUpdatesResponse");
    final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
    switch (status) {
    case SUCCESSFUL:
//      Log.d(TAG, "[IAB] onPurchaseUpdatesResponseStatus: SUCCESSFUL");
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
      Editor editor = sharedPreferences.edit();
      editor.putBoolean("successIabRequest", true);
      editor.commit();
//      Log.d(TAG, "[IAB] Receipts size: " + response.getReceipts().size());
      
      boolean qualified = false;
      
      for (final Receipt receipt : response.getReceipts()) {
//        final String receiptId = receipt.getReceiptId();
        final String sku = receipt.getSku();
//        final ProductType productType = receipt.getProductType();
//        final Date purchaseDate = receipt.getPurchaseDate();
        final Date cancelDate = receipt.getCancelDate();
        
//        Log.d(TAG, "[IAB] ReceiptId: " + receiptId);
//        Log.d(TAG, "[IAB] SKU: " + sku);
//        Log.d(TAG, "[IAB] ProductType: " + productType.toString());
//        Log.d(TAG, "[IAB] PurchaseDate: " + purchaseDate);
//        Log.d(TAG, "[IAB] CancelDate: " + cancelDate);
        
        if (sku.equals(SKU_PREMIUM) && cancelDate == null) {  // 購入済みだった場合
          qualified = true;
        }
      }
      
      if (qualified) {  // 権利あり
        if (!parameterManager.getEdition()) {  // データとして保存されていない場合
          parameterManager.putEdition(true);
          activity.showReloadActivityDialog();  // 再描画
        }
      } else {  // なし
        if (parameterManager.getEdition()) {
          parameterManager.putEdition(false);
          activity.showReloadActivityDialog();  // 再描画
        }
      }
      
      // 読み込みきれていない情報があれば、更に読み込む？
      if (response.hasMore()) {
        PurchasingService.getPurchaseUpdates(false);
      }
      break;

    case FAILED:
    case NOT_SUPPORTED:
//      Log.d(TAG, "[IAB] onPurchaseUpdatesResponseStatus: FAILED");
      break;
    }
  }

  /**
   * onPurchaseResponse --- 購入情報の取得 (自前サーバなどで処理させたりするのに使う様。)
   */
  @Override
  public void onPurchaseResponse(final PurchaseResponse response) {
//    Log.d(TAG, "[IAB] onPurchaseResponse");
    Receipt receipt = null;
    switch(response.getRequestStatus()) {
    case SUCCESSFUL:  // 購入完了
//      Log.d(TAG, "[IAB] onPurchaseResponseStatus: SUCCESSFUL");
      receipt = response.getReceipt();
//      String receiptId = receipt.getReceiptId();
//      String userId = response.getUserData().getUserId();
      if (receipt.getSku().equals(SKU_PREMIUM)) {  // 購入確認が取れたので
        parameterManager.putEdition(true);
        activity.showReloadActivityDialog();  // 再描画
      }
      break;

    case ALREADY_PURCHASED:  // 購入済み
//      Log.d(TAG, "[IAB] onPurchaseResponseStatus: ALREADY_PURCHASED");
//      receipt = response.getReceipt();
//      if (receipt.getSku().equals(SKU_PREMIUM)) {  // 購入確認が取れたので
//        parameterManager.putEdition(true);
//        activity.showReloadActivityDialog();  // 再描画
//      }
      break;
      
    case FAILED:  // 失敗
    case INVALID_SKU:  // 無効な SKU
    case NOT_SUPPORTED:
//      showPurchaseError();
      break;
    }
  }

  
//  /**
//   * 購入時のエラーダイアログ
//   */
//  private void showPurchaseError() {
//    Resources res = activity.getResources();
//    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
//    alertDialogBuilder.setIcon(R.drawable.ic_launcher);
//    alertDialogBuilder.setTitle(res.getText(R.string.app_name));
//    alertDialogBuilder.setMessage(res.getText(R.string.purchase_error_alert_text));
//    alertDialogBuilder.setPositiveButton(res.getText(R.string.setting_ok),
//        new DialogInterface.OnClickListener() {
//      @Override
//      public void onClick(DialogInterface dialog, int which) {
//      }
//    });
//    alertDialogBuilder.setCancelable(true);
//    AlertDialog alertDialog = alertDialogBuilder.create();
//    alertDialog.setCanceledOnTouchOutside(false);
//    alertDialog.show();
//  }
  
  
}
