package com.kma.OnThiBangLaiXe.Adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;

import java.io.File;
import java.util.List;

public class CauHoiAdapter extends RecyclerView.Adapter<CauHoiAdapter.ViewHolder> {

    private List<CauHoi> dsCauHoi;
    private Context context;
    private DBHandler db;

    public CauHoiAdapter(List<CauHoi> dsCauHoi, Context context) {
        this.context = context;
        this.dsCauHoi = dsCauHoi;
        db = new DBHandler(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cau_hoi, parent, false));
    }

    @Override
    public int getItemViewType(int position) { return position; }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CauHoi ch = dsCauHoi.get(position);
        holder.txtSoCauHoi.setText("Câu " + (position + 1) + "/" + dsCauHoi.size());
        holder.ivSave.setTag(0);

        if (ch.getLuu() == 1) {
            holder.ivSave.setImageResource(R.drawable.baseline_bookmark_24_green);
            holder.ivSave.setTag(1);
        }

        if (ch.getDaTraLoiDung() != 0) {
            holder.txtDungSai.setText("Đã học");
            if (ch.getDaTraLoiDung() == 1) {
                holder.ivDungSai.setImageResource(R.drawable.baseline_check_circle_16);
            } else if (ch.getDaTraLoiDung() == 2) {
                holder.ivDungSai.setImageResource(R.drawable.baseline_cancel_16);
            }
        } else {
            holder.txtDungSai.setText("Chưa học");
        }

        holder.txtNoiDungCauHoi.setText(ch.getNoiDung());
        holder.ivSave.setOnClickListener(view -> {
            if ((int) holder.ivSave.getTag() == 1) {
                holder.ivSave.setImageResource(R.drawable.baseline_bookmark_24);
                holder.ivSave.setTag(0);
                db.updateLuuLaiCauHoi(ch.getMaCH(), 0);
            } else {
                holder.ivSave.setImageResource(R.drawable.baseline_bookmark_24_green);
                holder.ivSave.setTag(1);
                db.updateLuuLaiCauHoi(ch.getMaCH(), 1);
            }
        });

        loadImage(ch.getHinhAnh(), holder.ivCauHoi);

        bindOption(holder, position, "A", holder.wrapA, holder.tvLetterA, holder.rbA, ch.getDapAnA());
        bindOption(holder, position, "B", holder.wrapB, holder.tvLetterB, holder.rbB, ch.getDapAnB());
        bindOption(holder, position, "C", holder.wrapC, holder.tvLetterC, holder.rbC, ch.getDapAnC());
        bindOption(holder, position, "D", holder.wrapD, holder.tvLetterD, holder.rbD, ch.getDapAnD());

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        holder.rvCauHoi.setLayoutManager(llm);
    }

    private void bindOption(ViewHolder holder, int position, String letter,
                            LinearLayout wrap, TextView tvLetter, RadioButton rb, String text) {
        if (text == null || text.equals("null") || text.isEmpty()) {
            wrap.setVisibility(View.GONE);
            return;
        }
        wrap.setVisibility(View.VISIBLE);
        rb.setText(text);
        rb.setOnCheckedChangeListener(null);
        rb.setChecked(false);
        setCardDefault(wrap, tvLetter);

        rb.setOnCheckedChangeListener((btn, checked) -> {
            if (!checked) return;
            setDapAn(holder, position, letter);
        });

        wrap.setOnClickListener(v -> {
            uncheckOthers(holder, letter);
            rb.setChecked(true);
        });
    }

    private void uncheckOthers(ViewHolder holder, String except) {
        if (!except.equals("A")) holder.rbA.setChecked(false);
        if (!except.equals("B")) holder.rbB.setChecked(false);
        if (!except.equals("C")) holder.rbC.setChecked(false);
        if (!except.equals("D")) holder.rbD.setChecked(false);
    }

    private void setDapAn(ViewHolder holder, int position, String value) {
        CauHoi ch = dsCauHoi.get(position);

        if (ch.getGiaiThich() != null && !ch.getGiaiThich().isEmpty() && !ch.getGiaiThich().equals("null")) {
            holder.txtGiaiThichCauHoi.setText(ch.getGiaiThich());
            holder.txtGiaiThichCauHoi.setVisibility(View.VISIBLE);
        }

        holder.wrapA.setClickable(false);
        holder.wrapB.setClickable(false);
        holder.wrapC.setClickable(false);
        holder.wrapD.setClickable(false);
        holder.rbA.setEnabled(false);
        holder.rbB.setEnabled(false);
        holder.rbC.setEnabled(false);
        holder.rbD.setEnabled(false);
        holder.txtDungSai.setText("Đã học");

        boolean correct = ch.getDapAnDung().equals(value);
        if (correct) {
            holder.ivDungSai.setImageResource(R.drawable.baseline_check_circle_16);
        } else {
            holder.ivDungSai.setImageResource(R.drawable.baseline_cancel_16);
            setCardForLetter(holder, value, false);
        }
        setCardForLetter(holder, ch.getDapAnDung(), true);

        int result = correct ? 1 : 2;
        ch.setDaTraLoiDung(result);
        for (CauHoi item : DanhSach.getDsCauHoi()) {
            if (item.getMaCH() == ch.getMaCH()) {
                item.setDaTraLoiDung(result);
                break;
            }
        }
        db.updateDaTraLoi(ch.getMaCH(), result);
    }

    private void setCardForLetter(ViewHolder holder, String letter, boolean correct) {
        LinearLayout wrap;
        TextView tvLetter;
        switch (letter) {
            case "A": wrap = holder.wrapA; tvLetter = holder.tvLetterA; break;
            case "B": wrap = holder.wrapB; tvLetter = holder.tvLetterB; break;
            case "C": wrap = holder.wrapC; tvLetter = holder.tvLetterC; break;
            case "D": wrap = holder.wrapD; tvLetter = holder.tvLetterD; break;
            default: return;
        }
        if (correct) setCardCorrect(wrap, tvLetter);
        else setCardWrong(wrap, tvLetter);
    }

    private void setCardDefault(LinearLayout wrap, TextView letter) {
        wrap.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_card_default));
        letter.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_letter_bg));
        letter.setTextColor(context.getColor(R.color.muted));
    }

    private void setCardCorrect(LinearLayout wrap, TextView letter) {
        wrap.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_card_correct));
        letter.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_letter_correct_bg));
        letter.setTextColor(context.getColor(R.color.white));
    }

    private void setCardWrong(LinearLayout wrap, TextView letter) {
        wrap.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_card_wrong));
        letter.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_letter_wrong_bg));
        letter.setTextColor(context.getColor(R.color.white));
    }

    private void loadImage(String hinhAnh, ImageView iv) {
        if (hinhAnh == null || hinhAnh.equals("null")) {
            iv.setVisibility(View.GONE);
            return;
        }
        File f = new File(new ContextWrapper(context).getDir("images", Context.MODE_PRIVATE), hinhAnh);
        if (!f.exists()) {
            iv.setVisibility(View.GONE);
            return;
        }
        iv.setVisibility(View.VISIBLE);
        Glide.with(context).load(f).into(iv);
    }

    @Override
    public int getItemCount() { return dsCauHoi.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtNoiDungCauHoi, txtGiaiThichCauHoi, txtSoCauHoi, txtDungSai;
        final RadioButton rbA, rbB, rbC, rbD;
        final LinearLayout wrapA, wrapB, wrapC, wrapD;
        final TextView tvLetterA, tvLetterB, tvLetterC, tvLetterD;
        final RecyclerView rvCauHoi;
        final ImageView ivDungSai, ivCauHoi, ivSave;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rvCauHoi           = itemView.findViewById(R.id.rvGiaiThichBienBao);
            txtSoCauHoi        = itemView.findViewById(R.id.txtSoCauHoi);
            txtDungSai         = itemView.findViewById(R.id.txtDungSai);
            txtNoiDungCauHoi   = itemView.findViewById(R.id.txtNoiDungCauHoi);
            txtGiaiThichCauHoi = itemView.findViewById(R.id.txtGiaiThichCauHoi);
            ivDungSai          = itemView.findViewById(R.id.ivDungSai);
            ivCauHoi           = itemView.findViewById(R.id.ivCauHoi);
            ivSave             = itemView.findViewById(R.id.ivSave);
            rbA                = itemView.findViewById(R.id.rbA);
            rbB                = itemView.findViewById(R.id.rbB);
            rbC                = itemView.findViewById(R.id.rbC);
            rbD                = itemView.findViewById(R.id.rbD);
            wrapA              = itemView.findViewById(R.id.wrapA);
            wrapB              = itemView.findViewById(R.id.wrapB);
            wrapC              = itemView.findViewById(R.id.wrapC);
            wrapD              = itemView.findViewById(R.id.wrapD);
            tvLetterA          = itemView.findViewById(R.id.tvLetterA);
            tvLetterB          = itemView.findViewById(R.id.tvLetterB);
            tvLetterC          = itemView.findViewById(R.id.tvLetterC);
            tvLetterD          = itemView.findViewById(R.id.tvLetterD);
        }
    }
}
