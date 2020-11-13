public class HomeListFragment extends BaseFragment {
  private ArrayList<QuListBean.DataBean> selectedList1 = new ArrayList<>();
    private ArrayList<QuListBean.DataBean> selectedList2 = new ArrayList<>();
    @OnClick({R.id.tvItem01, R.id.tvItem02, R.id.tvItem04})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tvItem01:
            //打开地区选择器
                SelectpRrovinceCityDlg.getInstance().setContext(getActivity())
                        .setFlag(tvItem01)
                        .setVisiableOfView(true)
                        .setCallback(new SelectpRrovinceCityDlg.Callback() {
                            @Override
                            public void onCallback(View flag, ArrayList<QuListBean.DataBean> areaBeans) {
                                if (areaBeans == null || areaBeans.size() == 0) {
                                    return;
                                }
                                selectedList1 = areaBeans;
                                HashSet<String> startCityCode = new HashSet<>();
                                StringBuffer bufferCityCode = new StringBuffer();
                                StringBuffer bufferAreaCode = new StringBuffer();
                                StringBuffer bufferProvinceCode = new StringBuffer();
                                StringBuffer bufferAreaName = new StringBuffer(); //所有名字
                                for (int i = 0; i < areaBeans.size(); i++) {
                                    if (areaBeans.get(i).getProvinceCode().equals(areaBeans.get(i).getCityCode())) {
                                        //全省code
                                        bufferProvinceCode.append(areaBeans.get(i).getProvinceCode());
                                        bufferProvinceCode.append(",");
                                        if (bufferAreaName.length() > 0) {
                                            bufferAreaName.append(","+areaBeans.get(i).getProvinceName());
                                        } else {
                                            bufferAreaName.append(areaBeans.get(i).getProvinceName());
                                        }

                                    } else {
                                        //非全省
                                        //区跟名字处理
                                        if (areaBeans.get(i).getCityCode() != areaBeans.get(i).getAreaCode()) {
                                            bufferAreaCode.append(areaBeans.get(i).getAreaCode());
                                            bufferAreaCode.append(",");
                                            if (bufferAreaName.length() > 0) {
                                                bufferAreaName.append(","+areaBeans.get(i).getArea());
                                            } else {
                                                bufferAreaName.append(areaBeans.get(i).getArea());
                                            }

                                        } else {
                                            //市处理
                                            boolean isSuc = startCityCode.add(areaBeans.get(i).getCityCode());
                                            if (isSuc) {
                                                bufferCityCode.append(areaBeans.get(i).getCityCode());
                                                bufferCityCode.append(",");
                                            }
                                            if (bufferAreaName.length() > 0) {
                                                bufferAreaName.append(","+areaBeans.get(i).getCityName());
                                            } else {
                                                bufferAreaName.append(areaBeans.get(i).getCityName());
                                            }
                                        }
                                    }
                                }
                                Log.d("times", "--bufferCityCode=" + bufferCityCode.toString() + ",--areaCode=" + bufferAreaCode + ",--provinceCode=" + bufferProvinceCode);
                                //最后逗号去除
                                String startPCode = bufferProvinceCode.toString();
                                if (!startPCode.isEmpty()) {
                                    if (startPCode.substring(startPCode.length() - 1).equals(",")) {
                                        startPCode = startPCode.substring(0, startPCode.length() - 1);
                                    }
                                }
                                //查询数据刷新列表
                                homeListRequestEnity.startCityCode = bufferCityCode.toString();
                                homeListRequestEnity.startAreaCode = bufferAreaCode.toString();
                                homeListRequestEnity.startProvinceCode = startPCode;

                                String areaName = bufferAreaName.toString();
                                if (areaName.length() > 3) {
                                    areaName = areaName.substring(0,3)+"...";
                                }
                                tvItem01.setText(areaName);
                                tvItem01.setRightDrawable(ContextCompat.getDrawable(getActivity(), R.mipmap.home_bottom_jt));
//                                getOrderList(1, reFreshLayout);
                                page = 1;
                                condition = 1;
                                begin = "";
                                getOrderList(page, reFreshLayout);
                            }
                        })
                        .setData(selectedList1)
                        .setShowBottom(true)
                        .show(getActivity().getSupportFragmentManager());
                break;
            }
    }
}
