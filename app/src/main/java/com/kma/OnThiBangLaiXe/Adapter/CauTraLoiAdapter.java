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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.kma.OnThiBangLaiXe.DBHandler;
import com.kma.OnThiBangLaiXe.Model.CauHoi;
import com.kma.OnThiBangLaiXe.Model.CauTraLoi;
import com.kma.OnThiBangLaiXe.Model.DanhSach;
import com.kma.OnThiBangLaiXe.R;
import com.kma.OnThiBangLaiXe.activity.ThiThuActivity;

import java.io.File;
import java.util.List;
import java.util.Objects;

/*
 * Mô tả file:
 * Adapter hiển thị từng câu hỏi trong quá trình thi thử hoặc xem lại bài đã thi.
 * File này quản lý lựa chọn đáp án, cập nhật menu câu hỏi, lưu câu hỏi yêu thích
 * và khi đã thi xong thì hiển thị đáp án đúng/sai cho người dùng.
 */
public class CauTraLoiAdapter extends RecyclerView.Adapter<CauTraLoiAdapter.ViewHolder> {

    private List<CauTraLoi> dsCauTraLoi;
    private Context context;
    boolean daThi;
    private DBHandler db;

    public CauTraLoiAdapter(List<CauTraLoi> dsCauTraLoi, Context context, boolean daThi) {
        this.context = context;
        this.dsCauTraLoi = dsCauTraLoi;
        this.daThi = daThi;
        db = new DBHandler(this.context);
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
        CauHoi ch = new CauHoi();
        CauTraLoi ctl = dsCauTraLoi.get(position);

        for (CauHoi cauHoi : DanhSach.getDsCauHoi()) {
            if (cauHoi.getMaCH() == ctl.getMaCH()) { ch = cauHoi; break; }
        }

        holder.ivSave.setTag(0);
        if (ch.getLuu() == 1) {
            holder.ivSave.setImageResource(R.drawable.baseline_bookmark_24_green);
            holder.ivSave.setTag(1);
        }

        if (daThi) {
            if (ctl.getDapAnChon() != null) {
                if (Objects.equals(ctl.getDapAnChon(), ch.getDapAnDung())) {
                    holder.txtDungSai.setText("Đúng");
                    holder.ivDungSai.setImageResource(R.drawable.baseline_check_circle_16);
                } else {
                    holder.txtDungSai.setText("Sai");
                    holder.ivDungSai.setImageResource(R.drawable.baseline_cancel_16);
                }
            } else {
                holder.txtDungSai.setText("Bỏ qua");
            }
        }

        CauHoi finalCh = ch;
        holder.ivSave.setOnClickListener(view -> {
            int tag = (int) view.getTag();
            if (tag == 1) {
                ((ImageView) view).setImageResource(R.drawable.baseline_bookmark_24);
                view.setTag(0);
                db.updateLuuLaiCauHoi(finalCh.getMaCH(), 0);
            } else {
                ((ImageView) view).setImageResource(R.drawable.baseline_bookmark_24_green);
                view.setTag(1);
                db.updateLuuLaiCauHoi(finalCh.getMaCH(), 1);
            }
        });

        holder.txtSoCauHoi.setText("Câu " + (position + 1) + "/" + dsCauTraLoi.size());
        holder.txtNoiDungCauHoi.setText(ch.getNoiDung());

        bindOption(holder, position, "A", holder.wrapA, holder.tvLetterA, holder.rbA,
                ch.getDapAnA(), ch.getDapAnDung(), ctl.getDapAnChon());
        bindOption(holder, position, "B", holder.wrapB, holder.tvLetterB, holder.rbB,
                ch.getDapAnB(), ch.getDapAnDung(), ctl.getDapAnChon());
        bindOption(holder, position, "C", holder.wrapC, holder.tvLetterC, holder.rbC,
                ch.getDapAnC(), ch.getDapAnDung(), ctl.getDapAnChon());
        bindOption(holder, position, "D", holder.wrapD, holder.tvLetterD, holder.rbD,
                ch.getDapAnD(), ch.getDapAnDung(), ctl.getDapAnChon());

        loadImage(ch.getHinhAnh(), holder.ivCauHoi);
    }

    private void bindOption(ViewHolder holder, int position, String letter,
                            LinearLayout wrap, TextView tvLetter, RadioButton rb,
                            String text, String correctLetter, String chosenLetter) {
        if (text == null || text.equals("null") || text.isEmpty()) {
            wrap.setVisibility(View.GONE);
            return;
        }

        wrap.setVisibility(View.VISIBLE);
        rb.setText(text);
        rb.setEnabled(!daThi);

        if (daThi) {
            if (Objects.equals(correctLetter, letter)) {
                setCardCorrect(wrap, tvLetter);
            } else if (Objects.equals(chosenLetter, letter)) {
                setCardWrong(wrap, tvLetter);
            } else {
                setCardDefault(wrap, tvLetter);
            }
            wrap.setOnClickListener(null);
        } else {
            rb.setOnCheckedChangeListener(null);
            rb.setChecked(false);
            rb.setClickable(false);
            rb.setFocusable(false);

            wrap.setOnClickListener(v -> {
                // Null tất cả listener trước để tránh cascade khi setChecked
                silenceAllRadios(holder);

                // Reset trạng thái radio
                holder.rbA.setChecked(false);
                holder.rbB.setChecked(false);
                holder.rbC.setChecked(false);
                holder.rbD.setChecked(false);
                rb.setChecked(true);

                // Cập nhật dữ liệu
                for (CauTraLoi c : DanhSach.getDsCauTraLoi()) {
                    if (c.getMaDeThi() == dsCauTraLoi.get(position).getMaDeThi()
                            && c.getMaCH() == dsCauTraLoi.get(position).getMaCH()) {
                        c.setDapAnChon(letter);
                        break;
                    }
                }
                ThiThuActivity.dsCauTraLoi.get(position).setDapAnChon(letter);
                ThiThuActivity.menuAdapter.notifyItemChanged(position);

                // Cập nhật visual
                resetAllCards(holder);
                setCardSelected(wrap, tvLetter);
            });

            // Restore visual state khi rebind câu đã trả lời
            if (Objects.equals(chosenLetter, letter)) {
                setCardSelected(wrap, tvLetter);
            } else {
                setCardDefault(wrap, tvLetter);
            }
        }
    }

    private void silenceAllRadios(ViewHolder holder) {
        holder.rbA.setOnCheckedChangeListener(null);
        holder.rbB.setOnCheckedChangeListener(null);
        holder.rbC.setOnCheckedChangeListener(null);
        holder.rbD.setOnCheckedChangeListener(null);
    }

    private void resetAllCards(ViewHolder holder) {
        setCardDefault(holder.wrapA, holder.tvLetterA);
        setCardDefault(holder.wrapB, holder.tvLetterB);
        setCardDefault(holder.wrapC, holder.tvLetterC);
        setCardDefault(holder.wrapD, holder.tvLetterD);
    }

    private void setCardDefault(LinearLayout wrap, TextView letter) {
        wrap.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_card_default));
        letter.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_letter_bg));
        letter.setTextColor(context.getColor(R.color.muted));
    }

    private void setCardSelected(LinearLayout wrap, TextView letter) {
        wrap.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_card_selected));
        letter.setBackground(AppCompatResources.getDrawable(context, R.drawable.answer_letter_selected_bg));
        letter.setTextColor(context.getColor(R.color.white));
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
    public int getItemCount() { return dsCauTraLoi.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtNoiDungCauHoi, txtSoCauHoi, txtDungSai;
        final RadioButton rbA, rbB, rbC, rbD;
        final LinearLayout wrapA, wrapB, wrapC, wrapD;
        final TextView tvLetterA, tvLetterB, tvLetterC, tvLetterD;
        final ImageView ivCauHoi, ivSave, ivDungSai;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSoCauHoi      = itemView.findViewById(R.id.txtSoCauHoi);
            txtNoiDungCauHoi = itemView.findViewById(R.id.txtNoiDungCauHoi);
            txtDungSai       = itemView.findViewById(R.id.txtDungSai);
            ivCauHoi         = itemView.findViewById(R.id.ivCauHoi);
            ivDungSai        = itemView.findViewById(R.id.ivDungSai);
            ivSave           = itemView.findViewById(R.id.ivSave);
            rbA              = itemView.findViewById(R.id.rbA);
            rbB              = itemView.findViewById(R.id.rbB);
            rbC              = itemView.findViewById(R.id.rbC);
            rbD              = itemView.findViewById(R.id.rbD);
            wrapA            = itemView.findViewById(R.id.wrapA);
            wrapB            = itemView.findViewById(R.id.wrapB);
            wrapC            = itemView.findViewById(R.id.wrapC);
            wrapD            = itemView.findViewById(R.id.wrapD);
            tvLetterA        = itemView.findViewById(R.id.tvLetterA);
            tvLetterB        = itemView.findViewById(R.id.tvLetterB);
            tvLetterC        = itemView.findViewById(R.id.tvLetterC);
            tvLetterD        = itemView.findViewById(R.id.tvLetterD);
        }
    }
}
