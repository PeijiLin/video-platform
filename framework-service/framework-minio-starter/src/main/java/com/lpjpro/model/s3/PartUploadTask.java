package com.lpjpro.model.s3;

import java.io.InputStream;

public class PartUploadTask {
        private Integer partNumber;
        private InputStream inputStream;
        private Long partSize;

        public Integer getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(Integer partNumber) {
            this.partNumber = partNumber;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public Long getPartSize() {
            return partSize;
        }

        public void setPartSize(Long partSize) {
            this.partSize = partSize;
        }
    }