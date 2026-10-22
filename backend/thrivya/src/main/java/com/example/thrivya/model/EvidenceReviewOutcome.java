package com.example.thrivya.model;

public enum EvidenceReviewOutcome {

    /*
     * Reviewed evidence supports the claim only
     * within the recorded supportedScope.
     */
    SUPPORTED,

    /*
     * Reviewed evidence supports some, but not all,
     * of the claim.
     */
    PARTIALLY_SUPPORTED,

    /*
     * Evidence does not provide enough information
     * to reach a support decision.
     *
     * This does not automatically mean the claim is false.
     */
    INSUFFICIENT,

    /*
     * Reviewer found evidence inconsistent with
     * the specific claim.
     */
    CONTRADICTED,

    /*
     * Review is deferred or further checks are needed.
     */
    NEEDS_REVIEW
}