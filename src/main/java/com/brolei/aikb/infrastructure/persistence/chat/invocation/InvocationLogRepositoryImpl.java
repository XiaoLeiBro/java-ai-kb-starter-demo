package com.brolei.aikb.infrastructure.persistence.chat.invocation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.brolei.aikb.domain.chat.model.InvocationLog;
import com.brolei.aikb.domain.chat.repository.InvocationLogRepository;
import com.brolei.aikb.domain.knowledge.model.KnowledgeBaseId;
import com.brolei.aikb.domain.user.model.UserId;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

/** 基于 MyBatis-Plus 的 {@link InvocationLogRepository} 实现. */
@Repository
public class InvocationLogRepositoryImpl implements InvocationLogRepository {

  private final InvocationLogMapper mapper;
  private final InvocationLogPoAssembler assembler;

  public InvocationLogRepositoryImpl(
      InvocationLogMapper mapper, InvocationLogPoAssembler assembler) {
    this.mapper = mapper;
    this.assembler = assembler;
  }

  @Override
  public InvocationLog save(InvocationLog invocationLog) {
    InvocationLogPo po = assembler.toPo(invocationLog);
    mapper.insert(po);
    return invocationLog;
  }

  @Override
  public List<InvocationLog> findByOwnerId(UserId ownerId) {
    List<InvocationLogPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<InvocationLogPo>()
                .eq(InvocationLogPo::getOwnerId, ownerId.value())
                .orderByDesc(InvocationLogPo::getCreatedAt));
    return poList.stream().map(assembler::toDomain).toList();
  }

  @Override
  public List<InvocationLog> findByOwnerIdAndKnowledgeBaseId(
      UserId ownerId, KnowledgeBaseId knowledgeBaseId) {
    List<InvocationLogPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<InvocationLogPo>()
                .eq(InvocationLogPo::getOwnerId, ownerId.value())
                .eq(InvocationLogPo::getKnowledgeBaseId, knowledgeBaseId.value())
                .orderByDesc(InvocationLogPo::getCreatedAt));
    return poList.stream().map(assembler::toDomain).toList();
  }

  @Override
  public List<InvocationLog> findByOwnerIdAndDateRange(
      UserId ownerId, Instant dateFrom, Instant dateTo) {
    List<InvocationLogPo> poList =
        mapper.selectList(
            new LambdaQueryWrapper<InvocationLogPo>()
                .eq(InvocationLogPo::getOwnerId, ownerId.value())
                .ge(InvocationLogPo::getCreatedAt, dateFrom)
                .le(InvocationLogPo::getCreatedAt, dateTo)
                .orderByDesc(InvocationLogPo::getCreatedAt));
    return poList.stream().map(assembler::toDomain).toList();
  }
}
