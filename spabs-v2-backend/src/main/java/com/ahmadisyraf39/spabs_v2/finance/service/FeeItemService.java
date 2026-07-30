package com.ahmadisyraf39.spabs_v2.finance.service;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeItemRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeItemResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeItem;
import com.ahmadisyraf39.spabs_v2.finance.mapper.FeeItemMapper;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeeItemService {

    private final FeeItemRepository feeItemRepository;
    private final FeeItemMapper feeItemMapper;

    public FeeItemResponse create(FeeItemRequest request) {
        FeeItem feeItem = feeItemMapper.toEntity(request);
        return feeItemMapper.toResponse(feeItemRepository.save(feeItem));
    }

    public FeeItemResponse getById(Long id) {
        return feeItemMapper.toResponse(findEntityById(id));
    }

    public List<FeeItemResponse> getAll() {
        return feeItemRepository.findAll().stream().map(feeItemMapper::toResponse).toList();
    }

    public FeeItemResponse update(Long id, FeeItemRequest request) {
        FeeItem feeItem = findEntityById(id);
        feeItemMapper.updateEntity(feeItem, request);
        return feeItemMapper.toResponse(feeItemRepository.save(feeItem));
    }

    public void delete(Long id) {
        feeItemRepository.delete(findEntityById(id));
    }

    private FeeItem findEntityById(Long id) {
        return feeItemRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeItem not found: " + id));
    }
}
