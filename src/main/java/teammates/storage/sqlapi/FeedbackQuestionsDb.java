package teammates.storage.sqlapi;

import java.util.List;
import java.util.UUID;

import teammates.common.datatransfer.FeedbackParticipantType;
import teammates.common.util.HibernateUtil;
import teammates.storage.sqlentity.Course;
import teammates.storage.sqlentity.FeedbackQuestion;
import teammates.storage.sqlentity.FeedbackSession;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

/**
 * Handles CRUD operations for feedback questions.
 *
 * @see FeedbackQuestion
 */
public final class FeedbackQuestionsDb extends EntitiesDb {

    private static final FeedbackQuestionsDb instance = new FeedbackQuestionsDb();

    private FeedbackQuestionsDb() {
        // prevent initialization
    }

    public static FeedbackQuestionsDb inst() {
        return instance;
    }

    /**
     * Creates a new feedback question.
     *
     * @return the created question
     */
    public FeedbackQuestion createFeedbackQuestion(FeedbackQuestion feedbackQuestion) {
        persist(feedbackQuestion);
        return feedbackQuestion;
    }

    /**
     * Gets a feedback question.
     *
     * @return null if not found
     */
    public FeedbackQuestion getFeedbackQuestion(UUID fqId) {
        assert fqId != null;

        return HibernateUtil.get(FeedbackQuestion.class, fqId);
    }

    /**
     * Gets all feedback questions of a session.
     */
    public List<FeedbackQuestion> getFeedbackQuestionsForSession(UUID fdId) {
        CriteriaBuilder cb = HibernateUtil.getCriteriaBuilder();
        CriteriaQuery<FeedbackQuestion> cq = cb.createQuery(FeedbackQuestion.class);
        Root<FeedbackQuestion> fqRoot = cq.from(FeedbackQuestion.class);
        Join<FeedbackQuestion, FeedbackSession> fqJoin = fqRoot.join("feedbackSession");
        cq.select(fqRoot).where(cb.equal(fqJoin.get("id"), fdId));
        return HibernateUtil.createQuery(cq).getResultList();
    }

    /**
     * Gets a list of feedback questions by {@code feedbackSession} and {@code giverType}.
     *
     * @return null if not found
     */
    public List<FeedbackQuestion> getFeedbackQuestionsForGiverType(
            FeedbackSession feedbackSession, FeedbackParticipantType giverType) {
        assert feedbackSession != null;
        assert giverType != null;

        CriteriaBuilder cb = HibernateUtil.getCriteriaBuilder();
        CriteriaQuery<FeedbackQuestion> cq = cb.createQuery(FeedbackQuestion.class);
        Root<FeedbackQuestion> root = cq.from(FeedbackQuestion.class);
        Join<FeedbackQuestion, FeedbackSession> fqJoin = root.join("feedbackSession");
        cq.select(root)
                .where(cb.and(
                        cb.equal(fqJoin.get("id"), feedbackSession.getId()),
                        cb.equal(root.get("giverType"), giverType)));
        return HibernateUtil.createQuery(cq).getResultList();
    }

    /**
     * Deletes a feedback question.
     */
    public void deleteFeedbackQuestion(UUID fqId) {
        assert fqId != null;

        FeedbackQuestion fq = getFeedbackQuestion(fqId);
        if (fq != null) {
            delete(fq);
        }
    }

    /**
     * Checks if there is any feedback questions in a session in a course for the given giver type.
     */
    public boolean hasFeedbackQuestionsForGiverType(
            String feedbackSessionName, String courseId, FeedbackParticipantType giverType) {
        CriteriaBuilder cb = HibernateUtil.getCriteriaBuilder();
        CriteriaQuery<FeedbackQuestion> cq = cb.createQuery(FeedbackQuestion.class);
        Root<FeedbackQuestion> root = cq.from(FeedbackQuestion.class);
        Join<FeedbackQuestion, FeedbackSession> fsJoin = root.join("feedbackSession");
        Join<FeedbackSession, Course> courseJoin = fsJoin.join("course");

        cq.select(root)
                .where(cb.and(
                        cb.equal(courseJoin.get("id"), courseId),
                        cb.equal(fsJoin.get("name"), feedbackSessionName),
                        cb.equal(root.get("giverType"), giverType)));
        return !HibernateUtil.createQuery(cq).getResultList().isEmpty();
    }
}
