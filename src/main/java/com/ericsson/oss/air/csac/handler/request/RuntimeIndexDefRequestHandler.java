/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.air.csac.handler.request;

import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.air.api.model.RtIndexContextFieldDto;
import com.ericsson.oss.air.api.model.RtIndexDefDto;
import com.ericsson.oss.air.api.model.RtIndexDefListDto;
import com.ericsson.oss.air.api.model.RtIndexInfoFieldDto;
import com.ericsson.oss.air.api.model.RtIndexSourceDto;
import com.ericsson.oss.air.api.model.RtIndexTargetDto;
import com.ericsson.oss.air.api.model.RtIndexValueFieldDto;
import com.ericsson.oss.air.api.model.RtIndexWriterDto;
import com.ericsson.oss.air.csac.model.runtime.index.DeployedIndexDefinitionDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexSourceDto;
import com.ericsson.oss.air.csac.model.runtime.index.IndexTargetDto;
import com.ericsson.oss.air.csac.repository.DeployedIndexDefinitionDao;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler for requests corresponding to runtime index definitions
 */
@Component
public class RuntimeIndexDefRequestHandler {

    @Autowired
    private DeployedIndexDefinitionDao deployedIndexDefinitionDao;

    private static RtIndexDefDto getRtIndexDef(final DeployedIndexDefinitionDto deployedIndexDefinition) {
        return new RtIndexDefDto()
                .name(deployedIndexDefinition.indexDefinitionName())
                .description(deployedIndexDefinition.indexDefinitionDescription())
                .source(getRtIndexSource(deployedIndexDefinition))
                .target(getRtIndexTarget(deployedIndexDefinition))
                .writers(getRtIndexWriters(deployedIndexDefinition));
    }

    private static RtIndexSourceDto getRtIndexSource(final DeployedIndexDefinitionDto deployedIndexDefinition) {
        final IndexSourceDto indexSourceDto = deployedIndexDefinition.indexSource();
        return new RtIndexSourceDto().name(indexSourceDto.getIndexSourceName()).description(indexSourceDto.getIndexSourceDescription())
                .type(RtIndexSourceDto.TypeEnum.fromValue(indexSourceDto.getIndexSourceType().getSourceType()));
    }

    private static RtIndexTargetDto getRtIndexTarget(final DeployedIndexDefinitionDto deployedIndexDefinition) {
        final IndexTargetDto indexTargetDto = deployedIndexDefinition.indexTarget();
        return new RtIndexTargetDto().name(indexTargetDto.getIndexTargetName()).displayName(indexTargetDto.getIndexTargetDisplayName())
                .description(indexTargetDto.getIndexTargetDescription());
    }

    private static List<@Valid RtIndexWriterDto> getRtIndexWriters(final DeployedIndexDefinitionDto deployedIndexDefinition) {
        return deployedIndexDefinition.indexWriters().stream().map(writer -> {
            final RtIndexWriterDto writerDto = new RtIndexWriterDto();
            writerDto.setName(writer.name());
            writerDto.setInputSchema(writer.inputSchema());

            writer.valueFieldList().forEach(valueFieldDto -> writerDto.addValueItem(
                    new RtIndexValueFieldDto().name(valueFieldDto.getName()).description(valueFieldDto.getDescription())
                            .displayName(valueFieldDto.getValueFieldDisplayName()).recordName(valueFieldDto.getRecordName())
                            .unit(valueFieldDto.getUnit()).type(valueFieldDto.getUnit())));

            writer.contextFieldList().forEach(contextFieldDto -> writerDto.addContextItem(
                    new RtIndexContextFieldDto().name(contextFieldDto.getName()).description(contextFieldDto.getDescription())
                            .displayName(contextFieldDto.getContextFieldDisplayName()).recordName(contextFieldDto.getRecordName())
                            .nameType(RtIndexContextFieldDto.NameTypeEnum.fromValue(contextFieldDto.getNameType().getNameType()))));

            writer.infoFieldList().forEach(infoFieldDto -> writerDto.addInfoItem(
                    new RtIndexInfoFieldDto().name(infoFieldDto.getName()).description(infoFieldDto.getDescription())
                            .type(RtIndexInfoFieldDto.TypeEnum.fromValue(infoFieldDto.getType().getInfoFieldType()))
                            .displayName(infoFieldDto.getInfoFieldDisplayName()).recordName(infoFieldDto.getRecordName())));

            return writerDto;

        }).collect(Collectors.toList());
    }

    /**
     * Retrieves all deployed index definitions. The returned list includes
     * <ul>
     *  <li>total number of deployed index definitions</li>
     *  <li>list of deployed index definitions</li>
     * </ul>
     *
     * @return {@link RtIndexDefListDto} object
     */
    public RtIndexDefListDto getRtIndexDefinitions() {
        final List<DeployedIndexDefinitionDto> deployedIndexDefinitions = this.deployedIndexDefinitionDao.stream().toList();
        return new RtIndexDefListDto()
                .total(deployedIndexDefinitions.size())
                .indexes(deployedIndexDefinitions.stream()
                        .map(RuntimeIndexDefRequestHandler::getRtIndexDef)
                        .collect(Collectors.toList())
                );
    }

}
