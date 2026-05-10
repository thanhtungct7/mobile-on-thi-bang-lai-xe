package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.ThiThuActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;

public class CauTraLoiAdapter extends RecyclerView.Adapter<CauTraLoiAdapter.ViewHolder>
{
    private List<CauTraLoi> dsCauTraLoi;
    private Context context;
    boolean daThi;
    private DBHandler db;

    public CauTraLoiAdapter(List<CauTraLoi> dsCauTraLoi, Context context, boolean daThi) {
        this.context = context;
        this.dsCauTraLoi = dsCauTraLoi;
        this.daThi = daThi;
        db=new DBHandler(this.context);
    }

    @NonNull
    @Override
    public CauTraLoiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CauTraLoiAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cau_hoi, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull CauTraLoiAdapter.ViewHolder holder,int position) {
        CauHoi ch = new CauHoi();

        CauTraLoi ctl = dsCauTraLoi.get(position);

        for (CauHoi cauHoi : DanhSach.getDsCauHoi())
        {
            if (cauHoi.getMaCH() == ctl.getMaCH())
            {
                ch = cauHoi;
            }
        }

        if(ch.getLuu()==1)
        {
            holder.ivSave.setImageResource(R.drawable.ico_save_gree);
            holder.ivSave.setTag(R.drawable.ico_save_gree);
        }

        if (daThi)
        {
            if (ctl.getDapAnChon() != null)
            {
                if (Objects.equals(ctl.getDapAnChon(), ch.getDapAnDung()))
                {
                    holder.txtDungSai.setText("Đúng");
                    holder.ivDungSai.setImageResource(R.drawable.baseline_check_circle_24);
                }
                else
                {
                    holder.txtDungSai.setText("Sai");
                    holder.ivDungSai.setImageResource(R.drawable.baseline_cancel_24);
                }
            }
            else
            {
                holder.txtDungSai.setText("Chưa trả lời");
            }
        }

        CauHoi finalCh = ch;
        holder.ivSave.setOnClickListener(view -> {
            ImageView imageView = (ImageView) view;
            assert(R.id.ivSave == imageView.getId());
            Integer integer = (Integer) imageView.getTag();
            integer = integer == null ? 0 : integer;
            if (integer == R.drawable.baseline_bookmark_24_green) {
                imageView.setImageResource(R.drawable.baseline_bookmark_24);
                imageView.setTag(R.drawable.baseline_bookmark_24);
                db.updateLuuLaiCauHoi(finalCh.getMaCH(),0);
            } else {
                imageView.setImageResource(R.drawable.baseline_bookmark_24_green);
                imageView.setTag(R.drawable.baseline_bookmark_24_green);
                db.updateLuuLaiCauHoi(finalCh.getMaCH(),1);
            }

        });
        holder.txtSoCauHoi.setText("Câu " + (position + 1) + "/" + dsCauTraLoi.size() + " câu |");

        holder.txtNoiDungCauHoi.setText(ch.getNoiDung());

        if (ch.getDapAnA() != null && !ch.getDapAnA().equals("null") && !ch.getDapAnA().isEmpty())
        {
            holder.rbA.setText(ch.getDapAnA());
            holder.rbA.setVisibility(View.VISIBLE);
            holder.rbA.setEnabled(!daThi);

            if (daThi)
            {
                if (Objects.equals(ch.getDapAnDung(), "A"))
                {
                    holder.rbA.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_correct));
                    holder.rbA.setTextColor(context.getColor(R.color.black));
                }
                else if (ctl.getDapAnChon() != null && Objects.equals(ctl.getDapAnChon(), "A"))
                {
                    holder.rbA.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_wrong));
                    holder.rbA.setTextColor(context.getColor(R.color.black));
                }
            }

            holder.rbA.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b)
                {
                    for (CauTraLoi check : DanhSach.getDsCauTraLoi())
                    {
                        if (check.getMaDeThi() == dsCauTraLoi.get(position).getMaDeThi()
                                && check.getMaCH() == dsCauTraLoi.get(position).getMaCH())
                        {
                            check.setDapAnChon("A");
                            ThiThuActivity.dsCauTraLoi.get(position).setDapAnChon("A");
                            ThiThuActivity.menuAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    setBackGround(holder);
                    holder.rbA.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_checked));
                }
            });
        }
        else { holder.rbA.setVisibility(View.GONE); }

        if (ch.getDapAnB() != null && !ch.getDapAnB().equals("null") && !ch.getDapAnB().isEmpty())
        {
            holder.rbB.setText(ch.getDapAnB());
            holder.rbB.setVisibility(View.VISIBLE);
            holder.rbB.setEnabled(!daThi);

            if (daThi)
            {
                if (Objects.equals(ch.getDapAnDung(), "B"))
                {
                    holder.rbB.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_correct));
                    holder.rbB.setTextColor(context.getColor(R.color.black));
                }
                else if (ctl.getDapAnChon() != null && Objects.equals(ctl.getDapAnChon(), "B"))
                {
                    holder.rbB.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_wrong));
                    holder.rbB.setTextColor(context.getColor(R.color.black));
                }
            }

            holder.rbB.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b)
                {
                    for (CauTraLoi check : DanhSach.getDsCauTraLoi())
                    {
                        if (check.getMaDeThi() == dsCauTraLoi.get(position).getMaDeThi()
                                && check.getMaCH() == dsCauTraLoi.get(position).getMaCH())
                        {
                            check.setDapAnChon("B");
                            ThiThuActivity.dsCauTraLoi.get(position).setDapAnChon("B");
                            ThiThuActivity.menuAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    setBackGround(holder);
                    holder.rbB.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_checked));
                }
            });
        }
        else { holder.rbB.setVisibility(View.GONE); }

        if (ch.getDapAnC() != null && !ch.getDapAnC().equals("null") && !ch.getDapAnC().isEmpty())
        {
            holder.rbC.setText(ch.getDapAnC());
            holder.rbC.setVisibility(View.VISIBLE);
            holder.rbC.setEnabled(!daThi);

            if (daThi)
            {
                if (Objects.equals(ch.getDapAnDung(), "C"))
                {
                    holder.rbC.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_correct));
                    holder.rbC.setTextColor(context.getColor(R.color.black));
                }
                else if (ctl.getDapAnChon() != null && Objects.equals(ctl.getDapAnChon(), "C"))
                {
                    holder.rbC.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_wrong));
                    holder.rbC.setTextColor(context.getColor(R.color.black));
                }
            }

            holder.rbC.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b)
                {
                    for (CauTraLoi check : DanhSach.getDsCauTraLoi())
                    {
                        if (check.getMaDeThi() == dsCauTraLoi.get(position).getMaDeThi()
                                && check.getMaCH() == dsCauTraLoi.get(position).getMaCH())
                        {
                            check.setDapAnChon("C");
                            ThiThuActivity.dsCauTraLoi.get(position).setDapAnChon("C");
                            ThiThuActivity.menuAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    setBackGround(holder);
                    holder.rbC.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_checked));
                }
            });
        }
        else { holder.rbC.setVisibility(View.GONE); }

        if (ch.getDapAnD() != null && !ch.getDapAnD().equals("null") && !ch.getDapAnD().isEmpty())
        {
            holder.rbD.setText(ch.getDapAnD());
            holder.rbD.setVisibility(View.VISIBLE);
            holder.rbD.setEnabled(!daThi);

            if (daThi)
            {
                if (Objects.equals(ch.getDapAnDung(), "D"))
                {
                    holder.rbD.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_correct));
                    holder.rbD.setTextColor(context.getColor(R.color.black));
                }
                else if (ctl.getDapAnChon() != null && Objects.equals(ctl.getDapAnChon(), "D"))
                {
                    holder.rbD.setBackground(AppCompatResources.getDrawable(context,
                            R.drawable.radio_button_background_shape_wrong));
                    holder.rbD.setTextColor(context.getColor(R.color.black));
                }
            }

            holder.rbD.setOnCheckedChangeListener((compoundButton, b) -> {
                for (CauTraLoi check : DanhSach.getDsCauTraLoi())
                {
                    if (check.getMaDeThi() == dsCauTraLoi.get(position).getMaDeThi()
                            && check.getMaCH() == dsCauTraLoi.get(position).getMaCH())
                    {
                        check.setDapAnChon("D");
                        ThiThuActivity.dsCauTraLoi.get(position).setDapAnChon("D");
                        ThiThuActivity.menuAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                setBackGround(holder);
                holder.rbD.setBackground(AppCompatResources.getDrawable(context,
                        R.drawable.radio_button_background_shape_checked));
            });
        }
        else { holder.rbD.setVisibility(View.GONE); }
        loadImage(ch.getHinhAnh(), holder.ivCauHoi);
    }

    private void loadImage(String hinhAnh, ImageView iv) {
        if (hinhAnh == null || hinhAnh.equals("null")) {
            iv.setVisibility(View.GONE);
            return;
        }
        try {
            File f = new File(new ContextWrapper(context).getDir("images", Context.MODE_PRIVATE), hinhAnh);
            Log.d("IMG_DEBUG", "Load ảnh: " + f.getAbsolutePath() + " | tồn tại=" + f.exists());
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            if (b != null) {
                iv.setImageBitmap(b);
                iv.setVisibility(View.VISIBLE);
            } else {
                Log.e("IMG_DEBUG", "Decode null: " + hinhAnh);
                iv.setVisibility(View.GONE);
            }
        } catch (FileNotFoundException e) {
            Log.e("IMG_DEBUG", "Không tìm thấy file: " + hinhAnh);
            iv.setVisibility(View.GONE);
        }
    }

    private void setBackGround(CauTraLoiAdapter.ViewHolder holder)
    {
        holder.rbA.setBackground(AppCompatResources.getDrawable(context,
                R.drawable.radio_button_background_shape));
        holder.rbB.setBackground(AppCompatResources.getDrawable(context,
                R.drawable.radio_button_background_shape));
        holder.rbC.setBackground(AppCompatResources.getDrawable(context,
                R.drawable.radio_button_background_shape));
        holder.rbD.setBackground(AppCompatResources.getDrawable(context,
                R.drawable.radio_button_background_shape));
    }

    @Override
    public int getItemCount() {
        return dsCauTraLoi.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView txtNoiDungCauHoi, txtSoCauHoi, txtDungSai;
        private final RadioButton rbA, rbB, rbC, rbD;
        private final ImageView  ivCauHoi, ivSave, ivDungSai;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSoCauHoi = itemView.findViewById(R.id.txtSoCauHoi);
            txtNoiDungCauHoi = itemView.findViewById(R.id.txtNoiDungCauHoi);
            txtDungSai = itemView.findViewById(R.id.txtDungSai);
            ivCauHoi = itemView.findViewById(R.id.ivCauHoi);
            ivDungSai = itemView.findViewById(R.id.ivDungSai);
            ivSave=itemView.findViewById(R.id.ivSave);
            rbA = itemView.findViewById(R.id.rbA);
            rbB = itemView.findViewById(R.id.rbB);
            rbC = itemView.findViewById(R.id.rbC);
            rbD = itemView.findViewById(R.id.rbD);
        }
    }
}
