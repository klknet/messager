package com.konglk.ims.service;

import com.konglk.ims.domain.CollectDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.repo.ICollectRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by konglk on 2019/8/14.
 */
@Service
public class CollectService {

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private ICollectRepository collectRepository;
    @Autowired
    private MessageService messageService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 可收藏文字、图片、文件
     * @param userId
     * @param id
     */
    public void insert(String userId, String id) {
        MessageDO messageDO = messageService.findByMsgId(id);
        if (messageDO == null)
            return;
        if (collectRepository.existsByUserIdAndContent(userId, messageDO.getContent())) {
            logger.error("already collect this");
            return;
        }
        CollectDO collectDO = new CollectDO();
        collectDO.setType(messageDO.getType());
        collectDO.setUserId(userId);
        collectDO.setContent(messageDO.getContent());
        if (messageDO.getType() > 0) {
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(messageDO.getContent())));
            collectDO.setContentType(gridFSFile.getMetadata().getString("_contentType"));
            collectDO.setSize(gridFSFile.getLength());
            collectDO.setCreateTime(gridFSFile.getUploadDate());
            collectDO.setFilename(gridFSFile.getFilename());
        }else if (messageDO.getType() == 0) {
            collectDO.setCreateTime(messageDO.getCreateTime());
        }
        collectRepository.insert(collectDO);
    }

    public List<CollectDO> list(String userId) {
        return collectRepository.findByUserId(userId);
    }

}
