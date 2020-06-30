public class DialogUtil {
    private static final String TAG = "DialogUtil";
    
    public static void showTypeDialog(){
        AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext);
        listDialog.setTitle("选择审批类型");
//        listDialog.setItems();
        ArrayApplyAdapter adapter = new ArrayApplyAdapter(mContext, R.layout.apply_child, deviceBeans);
        adapter.setOnItemClickListener(new ArrayApplyAdapter.onAItemClickListener() {
            @Override
            public void onItemClick(int position) {
              Log.e(TAG, "22222  ArrayApplyAdapter onItemClick position="+position);
            }

            @Override
            public void onChildItemClick(int position) {
                Log.e(TAG, "22222  ArrayApplyAdapter onChildItemClick position="+position);
            }
        });
        
        listDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.e(TAG, "i="+i);
            }
        });
        listDialog.show();
    }
}
