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

public class Bullet implements Serializable {
    // 使用しないが，定義しておくと，クラスの中身が変更されていた場合に例外を発生できる
    private static final long serialVersionUID = 1255752248513019027L;
    private String mPackageName;
    private byte[] mBitmapByte;

    public Bullet(String packageName, byte[] bitmapByte) {
        this.mPackageName = packageName;
        this.mBitmapByte = bitmapByte;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public byte[] getBitmapByte() {
        return mBitmapByte;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public void setBitmapByte(byte[] bitmapByte) {
        this.mBitmapByte = bitmapByte;
    }
}
