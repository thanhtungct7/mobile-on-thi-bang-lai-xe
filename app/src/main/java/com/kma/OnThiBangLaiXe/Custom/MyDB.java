package com.kma.OnThiBangLaiXe.Custom;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;

import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.MainActivity;
import com.kma.OnThiBangLaiXe.Model.BienBao;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.Model.DeThi;
import com.kma.OnThiBangLaiXe.Model.LoaiBang;
import com.kma.OnThiBangLaiXe.Model.LoaiCauHoi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/*
 * Mô tả file:
 * Lớp hỗ trợ đồng bộ dữ liệu ứng dụng với Firebase Realtime Database và Firebase Storage.
 * File này kiểm tra phiên bản dữ liệu, tải ảnh câu hỏi khi cần,
 * cập nhật dữ liệu cục bộ qua DBHandler và điều hướng vào MainActivity sau khi đồng bộ xong.
 */
public class MyDB {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    DatabaseReference csdlVersion = database.getReference("Version");
    public static ProgressDialog progressDialog;
    DBHandler dbHandler;
    ValueEventListener vel;
    Context context;

    public MyDB(Context context) {

        this.context = context;
        dbHandler = new DBHandler(context);
    }

    public void capNhatDatabase() {
        // Counter: chờ cả CauHoi lẫn CauTraLoi xong mới navigate
        final int[] pendingCount = {2};

        Runnable checkAndNavigate = () -> {
            pendingCount[0]--;
            if (pendingCount[0] <= 0) {
                progressDialog.dismiss();
                MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
                mySharedPreferences.putBooleanValue("KEY_FIRST_INSTALL", true);
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        };

        DatabaseReference csdlLoaiCauHoi = database.getReference("LoaiCauHoi");
        csdlLoaiCauHoi.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    LoaiCauHoi tlbb = ds.getValue(LoaiCauHoi.class);
                    if (tlbb != null) {
                        if (dbHandler.findLCHByID(tlbb.getMaLoaiCH())) {
                            dbHandler.updateLoaiCauHoi(tlbb);
                        } else {
                            dbHandler.insertLoaiCauHoi(tlbb);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        DatabaseReference csdlBienBao = database.getReference("BienBao");
        csdlBienBao.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    BienBao tlbb = ds.getValue(BienBao.class);
                    if (tlbb != null) {
                        if (dbHandler.findBBByID(tlbb.getMaBB())) {
                            dbHandler.updateBB(tlbb);
                        } else {
                            dbHandler.insertBB(tlbb);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        DatabaseReference csdlCauHoi = database.getReference("CauHoi").child(String.valueOf(DanhSach.getLoaiBang()));
        csdlCauHoi.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    CauHoi tlbb = ds.getValue(CauHoi.class);
                    if (tlbb != null) {
                        if (dbHandler.findCHByID(tlbb.getMaCH())) {
                            dbHandler.updateCauHoi(tlbb);
                        } else {
                            dbHandler.insertCauHoi(tlbb);
                        }
                    }
                }
                dbHandler.generateDeThiTuCauHoi(DanhSach.getLoaiBang());
                checkAndNavigate.run();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkAndNavigate.run();
            }
        });

        DatabaseReference csdlDeThi = database.getReference("DeThi");
        csdlDeThi.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    DeThi tlbb = ds.getValue(DeThi.class);
                    if (tlbb != null) {
                        if (dbHandler.finDDeThiByID(tlbb.getMaDeThi())) {
                            dbHandler.updateDeThi(tlbb);
                        } else {
                            dbHandler.insertDeThi(tlbb);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        DatabaseReference csdlCauTraLoi = database.getReference("CauTraLoi");
        csdlCauTraLoi.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    CauTraLoi tlbb = ds.getValue(CauTraLoi.class);
                    if (tlbb != null) {
                        if (dbHandler.findCauTraLoiByID(tlbb.getMaDeThi(), tlbb.getMaCH())) {
                            dbHandler.updateCauTraLoi(tlbb);
                        } else {
                            dbHandler.insertCauTraLoi(tlbb);
                        }
                    }
                }
                checkAndNavigate.run();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                checkAndNavigate.run();
            }
        });

        DatabaseReference cdslLoaiBang = database.getReference("Loaibang");
        cdslLoaiBang.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    LoaiBang tlbb = ds.getValue(LoaiBang.class);
                    if (tlbb != null) {
                        if (dbHandler.findLoaiBang(tlbb.getMaLoaiBang())) {
                            dbHandler.updateLoaiBang(tlbb);
                        } else {
                            dbHandler.insertLoaiBang(tlbb);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
    public void downloadWithBytes(String type) {
        StorageReference imageRefl = storageReference.child(type);
        imageRefl.listAll()
            .addOnSuccessListener(listResult -> {
                for (StorageReference sr : listResult.getItems()) {
                    ContextWrapper cw = new ContextWrapper(context);
                    File directory = cw.getDir("images", Context.MODE_PRIVATE);
                    File existing = new File(directory, sr.getName());
                    if (existing.exists()) continue;
                    long SIZE = 5 * 1024 * 1024;
                    sr.getBytes(SIZE)
                        .addOnSuccessListener(bytes -> storeImageBytes(bytes, sr.getName()));
                }
            });
    }

    public void downloadImagesIfNeeded() {
        String imageFolder = DanhSach.getLoaiBang() == 1 ? "anh_cau_hoi_a1" : "CauHoi";
        downloadWithBytes(imageFolder);
    }

    private void storeImageBytes(byte[] bytes, String name) {
        File directory = new ContextWrapper(context).getDir("images", Context.MODE_PRIVATE);
        File file = new File(directory, name);
        if (!file.exists()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
                fos.flush();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean kiemTraPhienBan() {
        final boolean[] isLastestVersion = {true};
        final int[] ver = {0};
        vel = csdlVersion.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isLastestVersion[0] = dbHandler.isLastestVersion(snapshot.getValue(int.class));
                if (!isLastestVersion[0]) {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("Loading....");
                    progressDialog.setMessage("quá trình này có thể mất vài phút,yêu cầu phải kết nối mạng...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    String imageFolder = DanhSach.getLoaiBang() == 1 ? "anh_cau_hoi_a1" : "CauHoi";
                    downloadWithBytes(imageFolder);
                    capNhatDatabase();
                    dbHandler.UpdateVersion(snapshot.getValue(int.class));
                    isLastestVersion[0] = true;
                } else if (!dbHandler.coCauHoiChoLoaiBang(DanhSach.getLoaiBang())) {
                    // Version khớp nhưng chưa có câu hỏi cho loại bằng này (vd: vừa đổi bằng)
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("Loading....");
                    progressDialog.setMessage("quá trình này có thể mất vài phút,yêu cầu phải kết nối mạng...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    String imageFolder = DanhSach.getLoaiBang() == 1 ? "anh_cau_hoi_a1" : "CauHoi";
                    downloadWithBytes(imageFolder);
                    capNhatDatabase();
                } else {
                    MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
                    mySharedPreferences.putBooleanValue("KEY_FIRST_INSTALL", true);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }

                stop();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                MySharedPreferences mySharedPreferences = new MySharedPreferences(context);
                mySharedPreferences.putBooleanValue("KEY_FIRST_INSTALL", true);
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
        return isLastestVersion[0];
    }

    private void stop() {
        csdlVersion.removeEventListener(vel);
    }
}
