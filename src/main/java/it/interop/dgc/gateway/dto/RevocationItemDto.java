package it.interop.dgc.gateway.dto;

import java.util.List;

import it.interop.dgc.gateway.model.RevocationBatch;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RevocationItemDto {


    private Boolean more;

    private List<RevocationBatch> batches;
    
}