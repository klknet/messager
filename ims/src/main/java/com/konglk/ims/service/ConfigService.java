package com.konglk.ims.service;

import com.konglk.ims.repo.IConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by konglk on 2019/9/11.
 */
@Service
public class ConfigService {
    @Autowired
    private IConfigRepository configRepository;

    @Transactional
    public void updateConfigValue(String name, String value) {
        configRepository.updateConfigValue(name, value);
    }
}
