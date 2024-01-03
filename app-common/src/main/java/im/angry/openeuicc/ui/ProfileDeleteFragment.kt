package im.angry.openeuicc.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import im.angry.openeuicc.common.R
import im.angry.openeuicc.util.preferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.typeblog.lpac_jni.LocalProfileNotification
import java.lang.Exception

class ProfileDeleteFragment : DialogFragment(), EuiccFragmentMarker {
    companion object {
        const val TAG = "ProfileDeleteFragment"

        fun newInstance(slotId: Int, portId: Int, iccid: String, name: String): ProfileDeleteFragment {
            val instance = newInstanceEuicc(ProfileDeleteFragment::class.java, slotId, portId)
            instance.requireArguments().apply {
                putString("iccid", iccid)
                putString("name", name)
            }
            return instance
        }
    }

    private var deleting = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme).apply {
            setMessage(getString(R.string.profile_delete_confirm, requireArguments().getString("name")))
            setPositiveButton(android.R.string.ok, null) // Set listener to null to prevent auto closing
            setNegativeButton(android.R.string.cancel, null)
        }.create()
    }

    override fun onResume() {
        super.onResume()
        val alertDialog = dialog!! as AlertDialog
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (!deleting) delete()
        }
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            if (!deleting) dismiss()
        }
    }

    private fun delete() {
        deleting = true
        val alertDialog = dialog!! as AlertDialog
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false

        lifecycleScope.launch {
            try {
                doDelete()
            } catch (e: Exception) {
                Log.d(ProfileDownloadFragment.TAG, "Error deleting profile")
                Log.d(ProfileDownloadFragment.TAG, Log.getStackTraceString(e))
            } finally {
                if (parentFragment is EuiccProfilesChangedListener) {
                    (parentFragment as EuiccProfilesChangedListener).onEuiccProfilesChanged()
                }
                dismiss()
            }
        }
    }

    private suspend fun doDelete() = channel.lpa.beginOperation {
        channel.lpa.deleteProfile(requireArguments().getString("iccid")!!)
        preferenceRepository.notificationDeleteFlow.first()
    }
}