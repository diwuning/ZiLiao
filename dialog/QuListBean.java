//只取区列表中的DataBean
public static class DataBean {
    /**
         * id : 1348
         * areaCode : 370101
         * area : 市辖区
         * cityCode : 370100
         */

        private String id;
        private String areaCode;
        private String area;
        private String cityCode;
        private String cityName;
        private String provinceCode;
        private String provinceName;

        public String getProvinceCode() {
            return provinceCode;
        }

        public void setProvinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
        }

        public String getProvinceName() {
            return provinceName;
        }

        public void setProvinceName(String provinceName) {
            this.provinceName = provinceName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getCityCode() {
            return cityCode;
        }

        public void setCityCode(String cityCode) {
            this.cityCode = cityCode;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataBean dataBean = (DataBean) o;
            return Objects.equals(id, dataBean.id) ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, areaCode, area, cityCode);
        }
}
