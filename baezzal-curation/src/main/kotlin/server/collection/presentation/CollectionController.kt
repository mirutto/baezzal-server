package server.collection.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.collection.application.CollectionDeleteResult
import server.collection.application.CollectionData
import server.collection.application.CollectionIdResult
import server.collection.application.CollectionService
import server.collection.application.CreateCollectionCommand
import server.collectionpost.application.AddCollectionPostCommand
import server.collectionpost.application.CollectionPostResult
import server.collectionpost.application.CollectionPostService

@RestController
@RequestMapping("/api/v1/collection")
class CollectionController(
    private val collectionService: CollectionService,
    private val collectionPostService: CollectionPostService,
) {
    @PostMapping
    fun create(
        @RequestPassport passport: Passport,
        @RequestBody command: CreateCollectionCommand,
    ): ApiResponse<CollectionIdResult> = ApiResponse.of(
        collectionService.create(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @GetMapping
    fun findAll(
        @RequestPassport passport: Passport,
    ): ApiResponse<List<CollectionData>> = ApiResponse.of(
        collectionService.findAllByMemberId(passport.memberId),
    )

    @PostMapping("/{collectionId}/post")
    fun addPost(
        @RequestPassport passport: Passport,
        @PathVariable collectionId: Long,
        @RequestBody command: AddCollectionPostCommand,
    ): ApiResponse<CollectionPostResult> = ApiResponse.of(
        collectionPostService.add(
            memberId = passport.memberId,
            collectionId = collectionId,
            command = command,
        ),
    )

    @DeleteMapping("/{collectionId}/post/{postId}")
    fun removePost(
        @RequestPassport passport: Passport,
        @PathVariable collectionId: Long,
        @PathVariable postId: Long,
    ): ApiResponse<CollectionPostResult> = ApiResponse.of(
        collectionPostService.remove(
            memberId = passport.memberId,
            collectionId = collectionId,
            postId = postId,
        ),
    )

    @DeleteMapping("/{collectionId}")
    fun delete(
        @RequestPassport passport: Passport,
        @PathVariable collectionId: Long,
    ): ApiResponse<CollectionDeleteResult> = ApiResponse.of(
        collectionService.delete(
            memberId = passport.memberId,
            collectionId = collectionId,
        ),
    )
}
