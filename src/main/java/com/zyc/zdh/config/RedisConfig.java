package com.zyc.zdh.config;

import com.zyc.zdh.cache.MyCacheManager;
import com.zyc.zdh.cache.MyCacheTemplate;
import com.zyc.zdh.cache.MyRedisCache;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;
import redis.clients.jedis.JedisPoolConfig;
//import redis.clients.jedis.JedisPoolConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClassName: RedisConfig
 *
 * @author zyc-admin
 * @date 2018年1月23日
 * @Description:
 */
@Configuration
@Data
public class RedisConfig {

	@Value("${redis.task.host}")
	private String redisHost;

	@Value("${redis.task.port}")
	private int redisPort;

	@Value("${redis.task.pass}")
	private String redisPass;

	@Value("${redis.task.db}")
	private int redisDb;

	@Value("${redis.task.config.timeout}")
	private int timeout;
	@Value("${redis.task.config.maxTotal}")
	private int maxTotal;
	@Value("${redis.task.config.maxIdle}")
	private int maxIdle;
	@Value("${redis.task.config.maxWaitMillis}")
	private int maxWaitMillis;
	@Value("${redis.task.config.minEvictableIdleTimeMillis}")
	private String minEvictableIdleTimeMillis;
	@Value("${redis.task.config.numTestsPerEvictionRun}")
	private int numTestsPerEvictionRun;
	@Value("${redis.task.config.timeBetweenEvictionRunsMillis}")
	private String timeBetweenEvictionRunsMillis;
	@Value("${redis.task.config.testOnBorrow}")
	private Boolean testOnBorrow;
	@Value("${redis.task.config.testWhileIdle}")
	private Boolean testWhileIdle;

	@Bean
	@Primary
	public RedisConnectionFactory taskConnectionFactory() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(maxIdle);
		jedisPoolConfig.setMaxTotal(maxTotal);
		jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
		jedisPoolConfig.setMinEvictableIdleTimeMillis(Integer.parseInt(minEvictableIdleTimeMillis));
		jedisPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
		jedisPoolConfig.setTimeBetweenEvictionRunsMillis(Integer.parseInt(timeBetweenEvictionRunsMillis));
		jedisPoolConfig.setTestOnBorrow(testOnBorrow);
		jedisPoolConfig.setTestWhileIdle(testWhileIdle);
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory(jedisPoolConfig);
		connectionFactory.setPort(redisPort);
		connectionFactory.setHostName(redisHost);
		connectionFactory.setDatabase(redisDb);
//        connectionFactory.setPassword(redisPass);
		//配置连接池属性
		connectionFactory.setTimeout(timeout);


		return connectionFactory;
	}

	@Bean
	public StringRedisTemplate taskRedisTemplate() {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(taskConnectionFactory());
		return template;
	}

	@Autowired
	Environment ev;

	@Bean("jedisPoolConfig")
	public JedisPoolConfig jedisPoolConfig() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(maxIdle);
		jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
		jedisPoolConfig.setTestOnBorrow(true);
		jedisPoolConfig.setTestWhileIdle(false);
		return jedisPoolConfig;
	}

	@Bean
	public JedisConnectionFactory redisConnectionFactory(
			JedisPoolConfig jedisPoolConfig) {
		// 如果集群使用new JedisConnectionFactory(new
		// RedisClusterConfiguration()),集群配置在RedisClusterConfiguration,这里省略具体配置
		JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
		if(redisHost.contains(",")){
			//redis 集群模式
			RedisClusterConfiguration rc=new RedisClusterConfiguration();
			for(String hp:redisHost.split(",")){
				RedisNode rn=new RedisNode(hp.split(":")[0],Integer.parseInt(hp.split(":")[1]));
				rc.addClusterNode(rn);
			}
			redisConnectionFactory=new JedisConnectionFactory(rc,jedisPoolConfig);
		}else{
			redisConnectionFactory.setHostName(redisHost);
			redisConnectionFactory.setPort(redisPort);
		}

		redisConnectionFactory.setPoolConfig(jedisPoolConfig);

		redisConnectionFactory.setTimeout(timeout);
		redisConnectionFactory.setPassword(redisPass);
		return redisConnectionFactory;
	}

	/**
	 * RedisTemplate配置
	 *
	 * @param redisConnectionFactory
	 * @return RedisTemplate
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			JedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		RedisSerializer<String> redisSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(redisSerializer);
		// Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new
		// Jackson2JsonRedisSerializer<Object>(
		// Object.class);
		// ObjectMapper om = new ObjectMapper();
		// om.setVisibility(PropertyAccessor.ALL,
		// JsonAutoDetect.Visibility.ANY);
		// om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		// jackson2JsonRedisSerializer.setObjectMapper(om);
		// redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
		redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
		return redisTemplate;
	}

	/**
	 * redis缓存管理器
	 * @param redisTemplate
	 * @return
	 */
	@Bean("redisCacheManager")
	public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate) {

		RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(Objects.requireNonNull(redisTemplate.getConnectionFactory()));
		//2.创建Jackson对象并传入需要序列化的对象
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		//3.传入 Jackson对象 并获取 RedisSerializationContext对象
		RedisSerializationContext<Object, Object> serializationContext = RedisSerializationContext.fromSerializer(serializer);
		//4.配置RedisCacheConfiguration
		/**
		 * RedisCacheConfiguration.defaultCacheConfig()
		 * 设置 value 的序列化 serializeValuesWit(SerializationPari<?> valueSerializationPari)
		 * 设置 key 的序列化 serializeKeysWith(SerializationPari valueSerializationPari)
		 */
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(serializationContext.getValueSerializationPair());
		//5.创建RedisCacheManager(RedisCacheWriter redisCacheWriter, RedisCacheConfiguration redisCacheConfiguration)对象并返回
		return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);



//		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
//		// Number of seconds before expiration. Defaults to unlimited (0)
//		cacheManager.setDefaultExpiration(120); //设置key-value超时时间
//		List<String> cacheNames = new ArrayList<>();
//		cacheNames.add("myRedis");
//		cacheNames.add("j2CacheRedis");
//		cacheManager.setCacheNames(cacheNames);
//		return cacheManager;
	}

	/**
	 * spring cache整合(EhCache,Redis)二级缓存具体Cache
	 * @param redisCacheManager
	 * @return
	 */
	@Bean
	@Primary
	public MyCacheTemplate myCacheTemplate(RedisCacheManager redisCacheManager){
		MyCacheTemplate myCacheTemplate=new MyCacheTemplate();
		myCacheTemplate.setEhCacheManager(ehCacheManagerFactoryBean().getObject());
		myCacheTemplate.setRedisCacheManager(redisCacheManager);
		myCacheTemplate.setName("j2CacheRedis");
		return myCacheTemplate;
	}

	/**
	 * 自定义redis缓存
	 * @param redisCacheManager
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public MyRedisCache myRedisCache(RedisCacheManager redisCacheManager,RedisTemplate<String,Object> redisTemplate){
		MyRedisCache myRedisCache=new MyRedisCache();
		//自定义属性配置缓存名称
		myRedisCache.setName("myRedis");
		//redis缓存管理器
		myRedisCache.setRedisCacheManager(redisCacheManager);
		//redisTemplate 实例
		myRedisCache.setRedisTemplate(redisTemplate);
		return myRedisCache;
	}

	/**
	 * spring cache 统一缓存管理器
	 * @return
	 */
	@Bean("cacheManager")
	@Primary
	public CacheManager cacheManager(){
		MyCacheManager cacheManager=new MyCacheManager();
		cacheManager.setMyCacheTemplate(myCacheTemplate(redisCacheManager(redisTemplate(redisConnectionFactory(jedisPoolConfig())))));
		cacheManager.setMyRedisCache(myRedisCache(redisCacheManager(redisTemplate(redisConnectionFactory(jedisPoolConfig()))),redisTemplate(redisConnectionFactory(jedisPoolConfig()))));
		List<String> cacheNames=new ArrayList<>();
		cacheNames.add("j2CacheRedis");
		cacheNames.add("myRedis");
		cacheManager.setCacheNames(cacheNames);
		return cacheManager;
	}

	// 整合ehcache
	@Bean
	public EhCacheCacheManager ehCacheCacheManager() {
		EhCacheCacheManager ehCacheCacheManager = new EhCacheCacheManager(ehCacheManagerFactoryBean().getObject());
		return ehCacheCacheManager;
	}


	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
		EhCacheManagerFactoryBean cacheManagerFactoryBean = new
				EhCacheManagerFactoryBean();
		//这里暂时借用shiro的ehcache配置文件
		//Resource r=new ClassPathResource("ehcache-shiro.xml");
		Resource r=new ClassPathResource(ev.getProperty("ecache.config.location"));
		cacheManagerFactoryBean.setConfigLocation(r);
		cacheManagerFactoryBean.setShared(true);
		return cacheManagerFactoryBean;
	}



}

