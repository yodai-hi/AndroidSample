package jp.naist.ubi_lab.androidsample.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.util.Log
import android.widget.Toast
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.EditText
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.naist.ubi_lab.androidsample.model.MemoData
import jp.naist.ubi_lab.androidsample.view.adapter.MemoViewAdapter


class HistoryDialogFragment : DialogFragment() {
    private val mTAG = "HistoryDialogFragment"
    private var id: Long? = null
    private var title: String? = null
    private var main: String? = null
    private var time: String? = null
    private var mRealm : Realm? = null
    private var mode: Boolean = true
    private var adapter: MemoViewAdapter? = null


    //外部から呼ばれたときにインスタンスを作製
    fun historyDialogFragment(memo: MemoData, mode: Boolean, adapter: MemoViewAdapter) {
        Log.d(mTAG, "historyDialogFragment is called")
        this.id = memo.id
        this.title = memo.title
        this.main = memo.main
        this.time = memo.time
        this.mode = mode
        this.adapter = adapter
    }


    //ダイアログが生成された時に呼ばれるメソッド ※必須
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if(mode){
            createViewDialog()
        }
        else{
            createEditDialog()
        }
    }

    //ViewモードのDialogを生成
    private fun createViewDialog() : Dialog{
        //ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化
        val dialogBuilder = AlertDialog.Builder(activity)
        //タイトル設定
        dialogBuilder.setTitle(title)
        //表示する文章設定
        dialogBuilder.setMessage(main)

        //Closeボタン作成
        dialogBuilder.setNegativeButton("Close") { _, _ ->
            //何もしないで閉じる
        }

        //dialogBuilderを返す
        return dialogBuilder.create()
    }

    //EditモードのDialogを生成
    private fun createEditDialog() : Dialog{
        //テキスト入力用Viewの作成
        val editTitleView = EditText(activity)
        editTitleView.setText(title)
        //書き込める行を1行に制限
        editTitleView.setLines(1)

        //テキスト入力用Viewの作成
        val editMainView = EditText(activity)
        editMainView.setText(main)
        //書き込める行を最大7行に制限
        editMainView.setLines(7)

        //ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化
        val dialogBuilder = AlertDialog.Builder(activity)
        //タイトル設定
        dialogBuilder.setCustomTitle(editTitleView)
        //表示する文章設定
        dialogBuilder.setView(editMainView)

        //Saveボタン作成
        dialogBuilder.setPositiveButton("Save"){ _, _ ->
            //トーストを出す
            Toast.makeText(activity, "Save edited memo", Toast.LENGTH_SHORT).show()
            //realm DBに変更を反映
            realmInit()
            updateMemo(id!!, editTitleView.text.toString(), editMainView.text.toString())
            adapter!!.notifyDataSetChanged()
            adapter!!.memoList = readMemo()
            mRealm!!.close()
        }

        //Cancelボタン作成
        dialogBuilder.setNegativeButton("Cancel"){ _, _ ->
            //何もしないで閉じる
        }

        //dialogBuilderを返す
        return dialogBuilder.create()
    }

    //realmのインスタンス生成
    private fun realmInit(){
        Realm.init(context!!)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
        mRealm = Realm.getInstance(realmConfig)
    }

    //realm DBの更新を行う関数
    private fun updateMemo(id:Long, title:String, main:String){
        mRealm!!.executeTransaction {
            val memo = mRealm!!.where(MemoData::class.java).equalTo("id",id).findFirst()
            memo!!.title = title
            memo.main = main
            memo.time = DateFormat.format("yyyy/MM/dd, kk:mm:ss", System.currentTimeMillis()).toString()
        }
    }

    //realmからMemoDataのDBを呼び出す
    private fun readMemo() : ArrayList<MemoData> {
        val memoRealm = mRealm!!.where(MemoData::class.java).findAll()
        val memoList = ArrayList<MemoData>()
        memoRealm.forEach {
            val memo = MemoData()
            memo.set(it.id!!, it.title!!, it.main!!, it.time!!)
            memoList.add(memo)
        }

        return memoList
    }
}
