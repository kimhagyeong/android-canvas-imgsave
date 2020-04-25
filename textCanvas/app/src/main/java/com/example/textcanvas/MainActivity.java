package com.example.textcanvas;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    Context context;
    Resources resources;
    RelativeLayout relativeLayout;
    Button button;
    ImageView imageView;
    String text="ttttttttttttttttttt\ndddddddddd\nhhhhhhhhhhhhhhhhhhhhhhhhh";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        resources = getResources();
        relativeLayout = findViewById(R.id.relativeLayout);
        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);


        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo2,options);

                Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(tempBitmap);

                Paint paint1 = new Paint();
                paint1.setARGB(80,204,204,204);

                TextPaint paint2 = new TextPaint();
                int textSize=bitmap.getWidth()/10;
                paint2.setTextSize(textSize);
                paint2.setColor(Color.LTGRAY);
                paint2.setAntiAlias(true);


                StaticLayout.Builder builder = StaticLayout.Builder.obtain(text, 0, text.length(), paint2,  bitmap.getWidth()-textSize );
                StaticLayout textLayout = builder.build();


                canvas.drawBitmap(bitmap, 0, 0, paint1);
                canvas.save();
                canvas.translate( textSize/2, textSize/2 );

                textLayout.draw(canvas);


                imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));


                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "fileName");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
                // 파일을 write중이라면 다른곳에서 데이터요구를 무시하겠다는 의미입니다.
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                ContentResolver contentResolver = getContentResolver();
                Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri item = contentResolver.insert(collection, values);

                try {
                    ParcelFileDescriptor pdf = contentResolver.openFileDescriptor(item, "w", null);
                    if (pdf == null) {

                    } else {
                        InputStream inputStream = getImageInputStream(tempBitmap);
                        byte[] strToByte = getBytes(inputStream);
                        FileOutputStream fos = new FileOutputStream(pdf.getFileDescriptor());
                        fos.write(strToByte);
                        fos.close();
                        inputStream.close();
                        pdf.close();
                        contentResolver.update(item, values, null, null);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                values.clear();
                // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                contentResolver.update(item, values, null, null);

            }
        });
    }
    private InputStream getImageInputStream(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte[] bitmapData = bytes.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);

        return bs;
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}