package it.interop.dgc.gateway.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RevocationItemDto {


    private Boolean more;

    private List<RevocationBatchListItemDto> batches;
    
}