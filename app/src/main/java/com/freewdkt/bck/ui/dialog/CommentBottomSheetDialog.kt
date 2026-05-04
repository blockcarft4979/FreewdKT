package com.freewdkt.bck.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewdkt.bck.databinding.DialogCommentBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogCommentBottomBinding? = null
    private val binding get() = _binding!!
    private var onCommentSendListener: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCommentBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.setOnClickListener {
            val content = binding.etComment.text.toString().trim()
            if (content.isNotEmpty()) {
                onCommentSendListener?.invoke(content)
                dismiss()
            }
        }
    }

    fun setOnCommentSendListener(listener: (String) -> Unit) {
        onCommentSendListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}