package com.revotech.meetingservice.business.meeting.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import com.revotech.dto.Organization
import com.revotech.meetingservice.business.meeting.MeetingService
import com.revotech.util.WebUtil
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = "organizations")
class OrganizationsDataLoader(private val meetingService: MeetingService, private val webUtil: WebUtil) :
    MappedBatchLoader<MeetingKeyLoader, List<Organization>> {
    override fun load(keys: MutableSet<MeetingKeyLoader>): CompletionStage<MutableMap<MeetingKeyLoader, List<Organization>>> {
        return CompletableFuture.supplyAsync {
            keys.associateWith { key ->
                webUtil.changeTenant(key.tenantId) {
                    meetingService.getOrganizations(key.tenantId, key.organizationCodes)
                }
            }.toMutableMap()
        }
    }
}