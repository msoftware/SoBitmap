/*
 * Copyright 2015 Kevin Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.airk.tool.sobitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Bitmap hunt request
 */
final class Request implements Callback, Runnable {
    final Context context;
    final String tag;
    final Uri source;
    final Options options;
    private final Callback callback;
    final Hunter target;
    final File cacheDir;
    Future<?> task;
    String key;
    HuntException e;
    int quality = 100;
    int recursionCount = 0;
    long startAllMs = -1;
    long startDecodeMs = -1;

    private final Handler handler;

    Request(Context context, String tag, Uri source, Options options, Callback callback, Hunter target, Handler handler, File dir) {
        this.context = context;
        if (tag == null) {
            this.tag = "sobitmap:request:" + Integer.toHexString(this.hashCode());
        } else {
            this.tag = tag;
        }
        this.source = source;
        this.options = options;
        this.callback = callback;
        this.target = target;
        this.handler = handler;
        this.cacheDir = dir;

        key = "KEY:" + source.toString() + "&&" + Integer.toHexString(options.hashCode());
    }

    @Override
    public String toString() {
        return "Request{ Key: " + key +
                "source: " + source.toString() +
                "with " + options.toString() + "}";
    }

    @Override
    public void onHunted(final Bitmap bitmap, final BitmapFactory.Options option) {
        notifyCallback();
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onHunted(bitmap, option);
            }
        });
    }

    @Override
    public void onException(final HuntException e) {
        notifyCallback();
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onException(e);
            }
        });
    }

    private void notifyCallback() {
        handler.obtainMessage(SoBitmap.MSG, tag).sendToTarget();
    }

    @Override
    public void run() {
        target.hunt(this);
    }
}
