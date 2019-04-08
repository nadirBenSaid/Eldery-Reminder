/*
 * Copyright (C) 2018 Bensaid Nadir (Bensaid.nadir@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.core.android.ensanotes.async;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import fr.core.android.ensanotes.ensaNotes;
import fr.core.android.ensanotes.models.Attachment;
import fr.core.android.ensanotes.models.listeners.OnAttachingFileListener;
import fr.core.android.ensanotes.utils.StorageHelper;

import java.lang.ref.WeakReference;


public class AttachmentTask extends AsyncTask<Void, Void, Attachment> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private OnAttachingFileListener mOnAttachingFileListener;
    private Uri uri;
    private String fileName;


    public AttachmentTask(Fragment mFragment, Uri uri, OnAttachingFileListener mOnAttachingFileListener) {
        this(mFragment, uri, null, mOnAttachingFileListener);
    }


    public AttachmentTask(Fragment mFragment, Uri uri, String fileName,
                          OnAttachingFileListener mOnAttachingFileListener) {
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.uri = uri;
        this.fileName = TextUtils.isEmpty(fileName) ? "" : fileName;
        this.mOnAttachingFileListener = mOnAttachingFileListener;
    }


    @Override
    protected Attachment doInBackground(Void... params) {
        return StorageHelper.createAttachmentFromUri(ensaNotes.getAppContext(), uri);
    }


    @Override
    protected void onPostExecute(Attachment mAttachment) {
        if (isAlive()) {
            if (mAttachment != null) {
                mOnAttachingFileListener.onAttachingFileFinished(mAttachment);
            } else {
                mOnAttachingFileListener.onAttachingFileErrorOccurred(null);
            }
        } else {
            if (mAttachment != null) {
                StorageHelper.delete(ensaNotes.getAppContext(), mAttachment.getUri().getPath());
            }
        }
    }


    private boolean isAlive() {
        return mFragmentWeakReference != null
                && mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing();
    }

}
