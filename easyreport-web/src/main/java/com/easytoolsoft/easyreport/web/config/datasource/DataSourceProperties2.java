package com.easytoolsoft.easyreport.web.config.datasource;

import org.apache.shiro.codec.Hex;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import static com.easytoolsoft.easyreport.common.crypto.EncyptUtils.decrypt;

/**
 * Created by yong.ma on 2019/5/16.
 */
public class DataSourceProperties2 extends DataSourceProperties {
    private static final String CIPHER_PREFX = "@";

    @Override
    public void setPassword(String password) {
        super.setPassword(decryptIfNeed(password));
    }

    @Override
    public void setUsername(String username) {
        super.setUsername(decryptIfNeed(username));
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(decryptIfNeed(url));
    }

    private String decryptIfNeed(String s) {
        if (!s.startsWith(CIPHER_PREFX)) {
            return s;
        }
        return new String(decrypt(Hex.decode(s.substring(1))));
    }
}
