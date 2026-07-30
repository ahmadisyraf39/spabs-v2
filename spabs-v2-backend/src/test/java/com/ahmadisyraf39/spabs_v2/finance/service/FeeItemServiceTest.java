package com.ahmadisyraf39.spabs_v2.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeItemRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeItemResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeItem;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FeeType;
import com.ahmadisyraf39.spabs_v2.finance.mapper.FeeItemMapper;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeeItemServiceTest {

    @Mock
    private FeeItemRepository feeItemRepository;

    @Mock
    private FeeItemMapper feeItemMapper;

    @InjectMocks
    private FeeItemService feeItemService;

    @Test
    void create_savesAndReturnsMappedResponse() {
        FeeItem entity = new FeeItem();
        when(feeItemMapper.toEntity(org.mockito.ArgumentMatchers.any())).thenReturn(entity);
        when(feeItemRepository.save(entity)).thenReturn(entity);
        when(feeItemMapper.toResponse(entity)).thenReturn(FeeItemResponse.builder().build());

        FeeItemRequest request = FeeItemRequest.builder()
                .name("Monthly Fee")
                .feeType(FeeType.MONTHLY)
                .amount(new BigDecimal("120.00"))
                .build();

        assertThat(feeItemService.create(request)).isNotNull();
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(feeItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feeItemService.getById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(feeItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> feeItemService.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
