package com.kindroid.hub.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ConvertUtils {
	
	public static byte[] drawableToByte(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
				.getIntrinsicHeight());
		drawable.draw(canvas);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	public static String inputSreamToString(InputStream in) throws IOException {
		StringBuffer put = new StringBuffer();
		byte[] size = new byte[2048];
		for (int length; (length = in.read(size)) != -1;) {
			put.append(new String(size, 0, length));
		}
		return put.toString();
	}
	
	public static byte[] InputStreamToByte(InputStream is) {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int len;
		try {
			while ((len = is.read(buffer)) > 0) {
				bytestream.write(buffer,   0,   len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte imgdata[] = bytestream.toByteArray();
		try {
			bytestream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imgdata;
	}
	
	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}
	
	public static byte[] bitmapToBytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	public static Bitmap resizeBitmap(Bitmap bitmap, float scale) {
		if (bitmap == null) {
			return null;
		}
		 //���getWidth��getHeight������ȡBitmap��Դ�Ŀ�͸�
        int bmpW = bitmap.getWidth();
        int bmpH = bitmap.getHeight();
        //����ͼƬ��С����
        
        float scaleW = 1;//��������ϵ��1��ʾ����
	    float scaleH = 1;//��������ϵ��1��ʾ����
        //��������Ҫ��С�ı���
        scaleW = (float)(scaleW * scale);
        scaleH = (float)(scaleH * scale);
        
        //����reSize���Bitmap����
        //ע�����Matirx��android.graphics���µ��Ǹ�
        Matrix mt = new Matrix();
        
        //��������ϵ��ֱ�Ϊԭ����0.8��0.8
        //�������Ϊ1��1������ԭ���ĳߴ�
        mt.postScale(scaleW, scaleH);
        
        //�������ϵ���ԭ����Bitmap��Դbm��������
        //����һΪԭ����Bitmap��Դbm
        //���������ΪBitmap������Ͻ����λ��
        //�����ĺ���Ϊԭ��Bitmap��Դbm�Ŀ�͸�
        //������Ϊ����ϵ�����
        //������Ϊ�Ƿ����
        //�õ����ź��Bitmapλͼ��Դ
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bmpW, bmpH, mt, true);
        
        return resizeBmp;
        
	}

}
