package com.gov.sindhpolice.beatbook.models;

public class PlacesListModel {
    public static class Item {
        private String name;
        private String place;
        private String detail;

        public Item(String name, String place, String detail) {
            this.name = name;
            this.place = place;
            this.detail = detail;
        }

        public String getName() {
            return name;
        }

        public String getPlace() {
            return place;
        }

        public String getDetail() {
            return detail;
        }
    }
}
