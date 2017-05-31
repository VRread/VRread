package de.fraunhofer.ipa.vrread.datasource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tbf on 31.05.2017.
 */

public class WebDatasource implements Datasource {

	final AtomicBoolean ready = new AtomicBoolean(false);

	private class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			ready.set(true);
		}
	}

	private WebView webview;

	public WebDatasource(Context ctx) {

		webview = new WebView(ctx);

		// Setup the webview to draw correctly.
		//webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		webview.setInitialScale(1);
		webview.enableSlowWholeDocumentDraw();

		// Test
		String RESULTDATA ="<html><body><h1>It's working</h1></body></html>";
		webview.loadData(RESULTDATA, "text/html", null);
	}

	public static String[] getSupportedMimeTypes() {
		return new String[]{"text/html"};
	}

	@Override
	public Bitmap getTextureBitmap(ReadPosition position, float scale, TextureSize size) {

		final Bitmap bitmap = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		webview.draw(canvas);

		/*
		try {
			signal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		return bitmap;
	}

	@Override
	public int getPageCount() {
		// HTML not really has a notion of pages.
		return 0;
	}

	@Override
	public boolean isInsidePage(ReadPosition tempReadPosition, float scale) {
		return false;
	}
}
