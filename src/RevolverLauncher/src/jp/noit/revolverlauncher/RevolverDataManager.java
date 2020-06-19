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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * リボルバーデータ管理クラス
 * @author naka
 *
 */
public class RevolverDataManager {
  
  // リボルバーデータを保存するファイル名
  private static final String FILENAME = "RevolverData.dat";

  // RevolverData.txtが作られていないか(最初の起動かどうか)を調べる鍵
  private static final String FIRSTTIME_KEY = "firsttime";

  // SharedPreferences
  private SharedPreferences sharedPreferences;

  // リボルバー(ページ)の数
  private int revolverNum = 0;

  // 弾数
  private int bulletNum = 0;

  // コンテキスト
  private Context context = null;

  public RevolverDataManager(Context context) {
    this.context = context;
  }
  
  public RevolverDataManager(Context context, int revolverNum, int bulletNum) {
    this.context = context;
    this.revolverNum = revolverNum;
    this.bulletNum = bulletNum;
  }

  // 弾数の変更
  public void changeBulletNum(int n) {
    SerializableRevolverData serializable = (SerializableRevolverData)load();
    serializable.changeChildSize(n);
    save(serializable);
  }
  
  // 連装数の変更
  public void changeRevolverNum(int n) {
    SerializableRevolverData serializable = (SerializableRevolverData)load();
    serializable.changeParentSize(n);
    save(serializable);
  }
  /**
   * ロード
   */
  public Serializable load() {

    // 保存ファイルが作られていないかを調べる
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    // SerializableRevolverData
    Serializable serializable = null;


    if (sharedPreferences.getBoolean(FIRSTTIME_KEY, true) == true) {  // 初回起動時
      // リボルバーデータを新規作成
      serializable = new SerializableRevolverData(revolverNum, bulletNum);
      save(serializable);

      Editor editor = sharedPreferences.edit();
      editor.putBoolean(FIRSTTIME_KEY, false);
      editor.commit();

    } else {

      // FileOutputStream
      FileInputStream fileInputStream = null;
      try {
        // ファイルの読み込み
        fileInputStream = context.openFileInput(FILENAME);

        // ObjectInputStream の生成
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        // データの取り出し
        serializable = (SerializableRevolverData) objectInputStream.readObject();

        // クローズ
        objectInputStream.close();

        return serializable;

      } catch (IOException e) {  // ファイルが存在しなかったとき
        e.printStackTrace();
        return null;
      } catch (ClassNotFoundException e) {  // クラスが見つからなかったとき
        e.printStackTrace();
        return null;
      } finally {  // 後処理
        try {
          if (fileInputStream != null) {
            fileInputStream.close();    // FileInputStream のクローズ
          }
        } catch (IOException e) {  // エラー処理
          e.printStackTrace();
        }
      }
    }
    return serializable;
  }

  /**
   * mRevolverData を RevolverData.txt に保存
   */
  public void save(Serializable serializable) {
    // FileOutputStream
    FileOutputStream fileOutputStream = null;

    try {
      // FileOutputStream
      fileOutputStream = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);

      // ObjectOutputStream
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

      // 書き出し
      objectOutputStream.writeObject(serializable);
      objectOutputStream.flush();
      objectOutputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    } finally {  // 後処理
      try {
        if (fileOutputStream != null) {
          fileOutputStream.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
