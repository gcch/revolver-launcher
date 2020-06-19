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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * StartOnBootBroadcastReceiver --- 端末起動時にスターターバーを起動するためのサービス
 * @author Tag
 *
 */
public class StartOnBootBroadcastReceiver extends BroadcastReceiver {

  private ParameterManager parameterManager;
  
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//      Log.d("StartOnBootBroadcastReceiver", "Receive ACTION_BOOT_COMPLETED");
      parameterManager = new ParameterManager(context);
      if (parameterManager.getBootServiceStatus() && parameterManager.getStarterBarServiceStatus()) {
        parameterManager.putStarterBarServiceStatus(true);
        Intent service = new Intent(context, AppsLauncherService.class);
        context.startService(service);
      }
    }
  }
  
}
