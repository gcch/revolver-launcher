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

import java.io.Serializable;
import java.util.ArrayList;

public class SerializableRevolverData implements Serializable {

    final String TAG = this.getClass().getName().toString();

    // 使用しないが，定義しておくと，クラスの中身が変更されていた場合に例外を発生できる
    private static final long serialVersionUID = 12345L;

    // 弾のリスト(リボルバー)のリスト(全てのリボルバー)
    private ArrayList<ArrayList<Bullet>> launcherList = new ArrayList<ArrayList<Bullet>>();

    /**
     * コンストラクタ
     * 
     * @param revolverNum リボルバーの数
     * @param bulletNum 弾の数
     */
    public SerializableRevolverData(int revolverNum, int bulletNum) {
        for (int i = 0; i < revolverNum; i++) {
            launcherList.add(new ArrayList<Bullet>());
            for (int j = 0; j < bulletNum; j++) {
                launcherList.get(i).add(new Bullet(null, null));
            }
        }
    }

    /**
     * アプリ名の保存
     * 
     * @param revolverNum
     * @param bulletNum
     * @param packageName
     */
    public void setPackageName(int revolverNum, int bulletNum, String packageName) {
        launcherList.get(revolverNum).get(bulletNum).setPackageName(packageName);
    }

    /**
     * アプリ名の取得
     * 
     * @param revolverNum
     * @param bulletNum
     * @return
     */
    public String getPackageName(int revolverNum, int bulletNum) {
        return launcherList.get(revolverNum).get(bulletNum).getPackageName();
    }

    /**
     * ビットマップデータの保存
     * 
     * @param revolverNum
     * @param bulletNum
     * @param BitmapByte
     */
    public void setBitmapByte(int revolverNum, int bulletNum, byte[] BitmapByte) {
        launcherList.get(revolverNum).get(bulletNum).setBitmapByte(BitmapByte);
    }

    /**
     * ビットマップデータの取得
     * 
     * @param revolverNum
     * @param bulletNum
     * @return
     */
    public byte[] getBitmapByte(int revolverNum, int bulletNum) {
        return launcherList.get(revolverNum).get(bulletNum).getBitmapByte();
    }

    /**
     * リボルバーの数の取得
     * 
     * @return
     */
    public int getSize() {
        return launcherList.size();
    }

    /**
     * リボルバーの数の変更 (不完全)
     */
    public void changeSize(int n) {
        for (int i = launcherList.size(); i >= n; i--) {
            launcherList.remove(i); // リストの削除
        }
    }

    /**
     * bulletNum 番目のリボルバーの弾数
     * 
     * @param bulletNum
     * @return
     */
    public int getChildSize(int revolverNum) {
        return launcherList.get(revolverNum).size();
    }

    /**
     * リボルバーの弾数の変更
     * 
     * @param n
     */
    public void changeChildSize(int n) {
//        Log.d(TAG, "changeChildSize(" + n + ")");
        int currentSize = launcherList.get(0).size();
        if (currentSize < n) {
//            Log.d(TAG, "increase");
            for (int i = 0; i < launcherList.size(); i++) {
                for (int j = launcherList.get(i).size(); j < n; j++) {
                    launcherList.get(i).add(new Bullet(null, null)); // 弾の追加
                }
            }
        } else {
//            Log.d(TAG, "decrease");
            for (int i = 0; i < launcherList.size(); i++) {
                for (int j = launcherList.get(i).size() - 1; j >= n; j--) {
                    launcherList.get(i).remove(j); // 弾の削除
                }
            }
        }
//        Log.d(TAG, "BulletNum: " + currentSize + "->" + launcherList.get(0).size());
    }

    /**
     * リボルバーの個数の変更
     * 
     * @param n
     */
    public void changeParentSize(int n) {
//        Log.d(TAG, "changeParentSize(" + n + ")");
        int currentSize = launcherList.size();
        if (currentSize < n) {
//            Log.d(TAG, "Revolver increase");
            for (int i = 0; i < n - currentSize; i++) {
                launcherList.add(new ArrayList<Bullet>()); // リボルバーの追加
                for (int j = 0; j < launcherList.get(0).size(); j++) {
                    launcherList.get(currentSize + i).add(new Bullet(null, null)); // 追加したリボルバーに弾の追加
                }
            }
        }
        else {
//            Log.d(TAG, "Revolver decrease");
            for (int i = 0; i < currentSize - n; i++) {
                launcherList.remove(currentSize - i - 1); // リボルバーの削除
            }

        }
//        Log.d(TAG, "RevolverNum: " + currentSize + "->" + launcherList.size());
    }

    public void clear() {
        launcherList.clear();
    }
}
