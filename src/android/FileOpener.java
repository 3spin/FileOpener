/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2011, IBM Corporation
 */

package com.phonegap.plugins.fileopener;

import java.io.IOException;
import java.net.URLConnection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import android.content.res.Resources;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.net.Uri;

import org.apache.cordova.CordovaPlugin;

public class FileOpener extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        try {
            if (action.equals("openFile")) {
                openFile(args.getString(0));
                callbackContext.success();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        } catch (RuntimeException e) {  // KLUDGE for Activity Not Found
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
        return false;
    }
    
    /**
     * The URI for a file path.
     *
     * @param {String} path
     *      The given path to the file
     *
     * @return The URI pointing to the given path
     */
    private Uri getUriForPath(String path) {
        if (path.startsWith("resource://")) {
            return getUriForResource(path);
        }
        
        return Uri.parse(path);
    }

    private void openFile(String url) throws IOException {
        // Create URI
        Uri uri = getUriForPath(url);

        Intent intent;
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type, 
        // so Android knew what application to use to open the file

        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/msword");
        } else if(url.contains(".pdf")) {
            // PDF file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.contains(".rtf")) {
            // RTF file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.contains(".wav")) {
            // WAV audio file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.contains(".gif")) {
            // GIF file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/gif");
        } else if(url.contains(".jpg") || url.contains(".jpeg")) {
            // JPG file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.contains(".txt")) {
            // Text file
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");
        } else if(url.contains(".mpg") || url.contains(".mpeg") || url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi")) {
            // Video files
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
        }
                
        //if you want you can also define the intent type for any other file
        
        //additionally use else clause below, to manage other unknown extensions
        //in this case, Android will show all applications installed on the device
        //so you can choose which application to use
        
        // else {
        //     intent = new Intent(Intent.ACTION_VIEW);
        //     intent.setDataAndType(uri, "*/*");
        // }

        else {
            String mimeType = URLConnection.guessContentTypeFromName(url);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
        }

        this.cordova.getActivity().startActivity(intent); // TODO handle ActivityNotFoundException
    }
    
    /**
     * The URI for a relative path based on the res folder
     *
     * @param {String} path
     *      The given relative path
     *
     * @return The URI pointing to the given path
     */
    private Uri getUriForResource(String path) {
        String resPath      = path.replaceFirst("resource://", "");
        String directory    = resPath.substring(0, resPath.lastIndexOf('/'));
        String fileName     = resPath.substring(resPath.lastIndexOf('/') + 1);
        String resName      = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension    = resPath.substring(resPath.lastIndexOf('.'));
        String storage      = cordova.getActivity().getExternalCacheDir().toString() + "/file_opener";

        Resources res       = cordova.getActivity().getResources();
        int resId           = res.getIdentifier(resName, directory, cordova.getActivity().getPackageName());
        File file           = new File(storage, resName + extension);

        if (resId == 0) {
            System.err.println("Resource not found: " + resPath);
        }

        new File(storage).mkdir();
        
        try {
            FileOutputStream outStream  = new FileOutputStream(file);
            InputStream inputStream     = res.openRawResource(resId);
            
            copyFile(inputStream, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return Uri.fromFile(file);
    }
    
    /**
     * Writes an InputStream to an OutputStream
     *
     * @param {InputStream} in
     * @param {OutputStream} out
     *
     * @return void
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    
}
