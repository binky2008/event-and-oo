package demo.command.domain.topic;

import java.util.ArrayList;
import java.util.List;

import demo.command.base.StateHandler;
import demo.command.base.EventBus;
import demo.command.base.EventFactory;
import demo.command.domain.blog.Blog;
import demo.command.domain.blog.BlogLikedEvent;
import demo.command.domain.blog.BlogStateUpdatedEvent;
import demo.command.domain.feed.ViewFeed;
import demo.command.domain.user.User;
import demo.command.domain.user.UserProfileUpdatedEvent;
import demo.infrastructure.DateUtil;
import demo.infrastructure.LogUtil;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Topicvent&oo
//  @ File Name : Topic.java
//  @ Date : 2013/9/27
//  @ Author : bner
//
//

public class Topic extends StateHandler<UserProfileUpdatedEvent> {

	private String id;

	private int likeCount;

	private List<String> blogIds = new ArrayList<String>();

	private transient List<ViewFeed> viewFeedList = new ArrayList<ViewFeed>();

	private transient TopicRepository repo = new TopicRepository();

	public Topic(String id) {
		this.id = id;

		EventBus.registHandler(UserProfileUpdatedEvent.class, this);
	}

	public String getId() {
		return id;
	}

	public void addBlog(Blog blog) {
		this.blogIds.add(blog.getId());
	}

	@Override
	public void handle(UserProfileUpdatedEvent event) {
		if (event.isOperator()) {
			BlogStateUpdatedEvent blogStateUpdatedEvent = event
					.getTheSource(BlogStateUpdatedEvent.class);
			if (blogStateUpdatedEvent != null
					&& this.blogIds.contains(blogStateUpdatedEvent.getBlogId())) {
				if (blogStateUpdatedEvent.getSource() instanceof BlogLikedEvent) {
					this.likeCount++;
				}
				TopicViewFeed viewFeed = new TopicViewFeed(event, event
						.getFeed(), this.id, event.getUserId());
				this.viewFeedList.add(viewFeed);

				LogUtil.print("Topic " + this.id + " receive Feed From Blog "
						+ blogStateUpdatedEvent.getBlogId() + " FeedId="
						+ event.getFeed());

				TopicStateUpdatedEvent topicStateUpdatedEvent = EventFactory
						.createEvent(TopicStateUpdatedEvent.class, event, this);
				topicStateUpdatedEvent.setTopicId(id);

				EventBus.send(topicStateUpdatedEvent);
			}
		}

	}

	public void subscribed(User user) {
		user.subscribe(this.id);
	}

	private TopicRepoVO getTopicRepoVO() {
		TopicRepoVO topicRepoVO = new TopicRepoVO();

		topicRepoVO.setId(id);
		topicRepoVO.setLikeCount(likeCount);
		topicRepoVO.setBlogIds(blogIds);
		topicRepoVO.setLastSaveDate(DateUtil.getSysDate());

		return topicRepoVO;

	}

	@Override
	public void load() {
		TopicRepoVO topicRepoVO = repo.find(id);

		this.likeCount = topicRepoVO.getLikeCount();
		this.blogIds = topicRepoVO.getBlogIds();

		this.setLastSaveDate(topicRepoVO.getLastSaveDate());

	}

	@Override
	public void save() throws Exception {
		repo.save(this.getTopicRepoVO());
	}

	@Override
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("\nTopic : ");
		info.append(id);
		info.append("\n");

		info.append("likeCount : ");
		info.append(likeCount);
		info.append("\n");

		info.append("blogIds : ");
		for (String blogId : blogIds) {
			info.append("\n");
			info.append(blogId);
		}
		info.append("\n");

		info.append("viewFeedList : ");
		for (ViewFeed feedView : viewFeedList) {
			info.append("\n");
			info.append(feedView);
		}
		info.append("\n");

		return info.toString();
	}
}
