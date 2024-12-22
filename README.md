# com.javagrunt.listener.youtube

## Reference Documentation

- [Subscribe to Push Notifications](https://developers.google.com/youtube/v3/guides/push_notifications)

## Steps to subscribe to the feed

- Go to [PubSubHubbub Hub](https://pubsubhubbub.appspot.com/subscribe)
- Enter the following details:
  - Callback URL: We used [https://javagrunt.com/youtube-listener/]
  - Topic URL: https://www.youtube.com/xml/feeds/videos.xml?channel_id=YOUR_CHANNEL_ID
  - Mode: Subscribe

## Example XML

```xml
<?xml version='1.0' encoding='UTF-8'?>
<feed
	xmlns:yt="http://www.youtube.com/xml/schemas/2015"
	xmlns="http://www.w3.org/2005/Atom">
	<link rel="hub" href="https://pubsubhubbub.appspot.com"/>
	<link rel="self" href="https://www.youtube.com/xml/feeds/videos.xml?channel_id=UCuGoHRQbVXa4LxepmPOdUfQ"/>
	<title>YouTube video feed</title>
	<updated>2023-12-16T20:59:59.795258257+00:00</updated>
	<entry>
		<id>yt:video:CTv7TyuQn-s</id>
		<yt:videoId>CTv7TyuQn-s</yt:videoId>
		<yt:channelId>UCuGoHRQbVXa4LxepmPOdUfQ</yt:channelId>
		<title>Spring Boot, YouTube Listener, then YouTube Analysis with Spring AI</title>
		<link rel="alternate" href="https://www.youtube.com/watch?v=CTv7TyuQn-s"/>
		<author>
			<name>DaShaun</name>
			<uri>https://www.youtube.com/channel/UCuGoHRQbVXa4LxepmPOdUfQ</uri>
		</author>
		<published>2023-12-15T22:15:34+00:00</published>
		<updated>2023-12-16T20:59:59.795258257+00:00</updated>
	</entry>
</feed>
``` 

## Thanks you!

Thank you [Jonas](https://github.com/Bjoggis-Studios) for the help!

[The blog he found that pointed to the fix!](https://kevincox.ca/2021/12/16/youtube-websub/#wrong-topic-url)