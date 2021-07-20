#### 1.Spring Security做了点啥？

##### 1.1 登录流程

基本上，绝大部分系统，都会涉及用户登录的流程。这个流程实际上是可以抽离出来的。这就是Spring Security的功能之一。
就目前而言，大部分系统的登录流程基本就是以下两种形式：

1. 前后端不分离，登录页调用登录接口后，接口直接给出跳转页
2. 前后端分离，登录页调用登录接口后，返回成功或者失败的信息。

但是不管1还是2，都绕不过一点，需要访问登录接口。

##### 1.2 权限认证

登录以后，客户端会访问后台的其它接口。但是接口是有权限的，没有权限，系统需要拒绝请求。这也是Spring Security的功能之一。

#### 2.Spring Security的配置

##### 2.1 相关代码

```java
@Configuration
@EnableWebSecurity
public class SecurityConfigORM extends WebSecurityConfigurerAdapter {

    @Autowired
//    private SecurityAuthenticationProvider securityAuthenticationProvider;

//    @Autowired
    private SecurityUserDetailsService securityUserDetailsService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        //静态资源放行，建议在这里配，这里是第一层。

        //不需要登录就可以访问静态资源
        web.ignoring().antMatchers("/img/**");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/box/**").hasIpAddress("127.0.0.1")//来自127.0.0.1的请求，如果匹配到"/box",那就可以不登录,直接访问
                .and().authorizeRequests()
                .anyRequest().authenticated()
                .and().formLogin()//浏览器中输入，http://localhost:8081/spring-boot-demo/login，默认登陆页
                //.loginPage("mylogin.html") //自定义登录页，使用自定义登陆界面以后，登出界面就要重新定义
                .loginProcessingUrl("/login")//登陆的post接口
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        //登陆成功处理，如果前后端分离，可以直接用于json返回

                        /**
                         * 1.自定义userDetailsService时，Principal = UserDetails
                         * 2.自定义AuthenticationProvider，Principal 由用户自定义。
                         */
                        request.getSession().setAttribute(Constants.SESSION_USER,authentication.getPrincipal());

                        String json = "{\"code\": 0,\"message\": \"ok\"}";
                        response.setContentType("text/json; charset=utf-8");
                        response.getWriter().print(JSON.toJSON(json));
                    }
                })
//                .defaultSuccessUrl("/")//登陆成功后的页面
//                .failureUrl("/login.html?error")//登陆失败后，统一跳转的页面，如果需要根据失败原因跳转不同页面，配置failureHandler
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

                        /**
                         * 登陆失败后的处理，可以
                         *    1.根据不同的错误原因跳转不同的页面
                         *    2.打印失败信息
                         *    3.统计失败次数
                         */

                        String msg = "error";
                        //根据失败原因跳转到不同的页面
                        if(e instanceof UsernameNotFoundException){
                            msg = e.getMessage();
                        }else if(e instanceof BadCredentialsException){
                            msg = e.getMessage();
                        }
                        String json = "{\"code\": 1,\"message\": \""+msg+"\"}";
                        response.setContentType("text/json; charset=utf-8");
                        response.getWriter().print(JSON.toJSON(json));
                    }
                })
                .and().logout().addLogoutHandler(new LogoutHandler() {//可以加好几个handler
            @Override
            public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                //在登出处理中，可以进行一些资源清理。
            }
        })
                .and().csrf().disable();//巨坑：重写configure以后，默认开启了csrf，拦截所有的post请求。如不配置csrf，必须关闭。

    }


    /**
     * 自定义userDetailsService时，需要配置
     */
    @Bean
    protected PasswordEncoder passwordEncoder(){

        return new BCryptPasswordEncoder(11);
    }


    /**
     *  想要使用数据库来进行验证。有以下几种方式：
     *  1.自定义userDetailsService，实现loadUserByUsername()方法，Spring Security默认使用DaoAuthenticationProvider
     *  2.自定义AuthenticationProvider，实现authenticate()方法。
     *
     *  需要注意的是：DaoAuthenticationProvider默认返回的是UsernamePasswordAuthenticationToken，
     *  它的principal，默认是自定义userDetailsService返回的对象，默认为UserDetails接口的子类
     *
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        //1.自定义userDetailsService，只控制从数据库获取用户的流程，默认流程走DaoAuthenticationProvider
        auth.userDetailsService(securityUserDetailsService);

        //2.自定义AuthenticationProvider，控制整个认证流程
//        auth.authenticationProvider(securityAuthenticationProvider);
    }
}
```

```java
@Component
public class SecurityAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal().toString();

        /** 问题：这个密码是明文还是密文？
         *  应该是密文，是没有进行过摘要算法的密文
         *  这个密文就是防程序员的
         */
        String rawPassword = authentication.getCredentials().toString();


        User user = userService.selectByUserName(username);

        /**
         * 需要从数据库中查询出权限，然后封装成GrantedAuthority
         */
        Collection<? extends GrantedAuthority> authorities = null;

        authentication.getDetails();
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在！");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (encoder.matches(rawPassword,user.getPassword())) {

            SessionUser sessionUser = new SessionUser(username);
            return new UsernamePasswordAuthenticationToken(sessionUser,user.getPassword(),authorities);
        }else {
            throw new BadCredentialsException("密码错误！！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
```

```java
@Component
public class SecurityUserDetailsService implements UserDetailsService, Serializable {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.selectByUserName(username);

        if (user == null) {
            return null;
        }

        return new SecurityUserDetail(user.getUsername(),user.getPassword(), Collections.singletonList(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "xxxx";
            }
        }));
    }
}
```

##### 2.2 通过数据库进行验证

通常来说，如果要通过数据库，使用ORM框架来验证，需要自定义UserDetailsService或者AuthenticationProvider。

上面的代码已经给出范例这里再重点强调下

###### 2.2.1 UserDetailsService

如果业务流程简单，只需要校验用户名和密码，而无需额外操作，那么整个登陆校验流程默认将使用DaoAuthenticationProvider，这个DaoAuthenticationProvider会调用UserDetailsService接口的loadUserByUsername方法。

所以我们只需要实现UserDetailsService接口，然后自定义loadUserByUsername方法，即可。在这个UserDetailsService中，我们可以注入自己原生的UserService

###### 2.2.2 AuthenticationProvider

如果认证流程比较复杂，可能需要进行签名校验之类的操作，那么这时候使用Spring Security自带的认证流程就不合适了。需要自定义认证流程。此时可以实现AuthenticationProvider接口，然后自定义authenticate方法。

此时，将替代Spring Security默认的DaoAuthenticationProvider。而在我们实现的AuthenticationProvider中，可以使用自己原生的UserService。

##### 2.3 csrf的坑

注意看代码的最后一行，csrf.disable()，这个必须写，否则会无法通过校验。

##### 2.4 FilterComparator

这个比较器，罗列了Spring Security全部的过滤器

#### 3.关于Session

##### 3.1 Session的独立性

Spring Security使用的Servlet规范的HttpRequest、HttpResponse和HttpSession，这些都是由web容器产生的。传递给Spring Security后，Spring Security也没有对其进行包裹或者封装。

所以，无论是使用Spring Session还是web容器的Session，对Spring Security都没关系，只要他们都符合Servlet规范。因为对Spring Security而言，他使用的是Servlet规范中的HttpRequest、HttpResponse和HttpSession接口，并不关心其具体实现。

##### 3.2 如何做到有Session时不再校验？

###### 3.2.1 问题描述

对于我们的系统而言，通常是这样的，访问一个接口：

1. 没有登录认证过，需要登录认证
2. 已经登录认证过了，会话存在则放行，会话过期则重新需要认证。

那么Spring Security是如何做的呢？

###### 3.2.2 UsernamePasswordAuthenticationFilter

Spring Security，本质上是一系列的Filter。

首先，所有请求都会经过UsernamePasswordAuthenticationFilter，它会判断。这次请求是不是登录请求。

如果是登录请求，就调用attemptAuthentication来进行登录验证。
如果不是登录请求，就继续走后面的Filter，但这并不意味着你可以直接绕过登录，随意访问其他接口。后续还是有判断的，这个后面讲。下面看具体代码。

```java
public abstract class AbstractAuthenticationProcessingFilter extends GenericFilterBean
      implements ApplicationEventPublisherAware, MessageSourceAware {
  
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (!requiresAuthentication(request, response)) { //判断是否是登录请求
			chain.doFilter(request, response);
			return;
		}
    
    //...一些日志输出

		Authentication authResult;

		try {
			authResult = attemptAuthentication(request, response);
			if (authResult == null) {
				// return immediately as subclass has indicated that it hasn't completed
				// authentication
				return;
			}
			sessionStrategy.onAuthentication(authResult, request, response);
		}
		catch (InternalAuthenticationServiceException failed) {
			logger.error(
					"An internal error occurred while trying to authenticate the user.",
					failed);
			unsuccessfulAuthentication(request, response, failed);

			return;
		}
		catch (AuthenticationException failed) {
			// Authentication failed
			unsuccessfulAuthentication(request, response, failed);

			return;
		}

		// Authentication success
		if (continueChainBeforeSuccessfulAuthentication) {
			chain.doFilter(request, response);
		}

		successfulAuthentication(request, response, chain, authResult);
	}
  
}
```

```java
public class UsernamePasswordAuthenticationFilter extends
      AbstractAuthenticationProcessingFilter {
   // ~ Static fields/initializers
   // =====================================================================================

   public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
   public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

   private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
   private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
   private boolean postOnly = true;

   // ~ Constructors
   // ===================================================================================================

   public UsernamePasswordAuthenticationFilter() {
      super(new AntPathRequestMatcher("/login", "POST"));
   }

   // ~ Methods
   // ========================================================================================================

   public Authentication attemptAuthentication(HttpServletRequest request,
         HttpServletResponse response) throws AuthenticationException {
      if (postOnly && !request.getMethod().equals("POST")) {
         throw new AuthenticationServiceException(
               "Authentication method not supported: " + request.getMethod());
      }

      String username = obtainUsername(request);
      String password = obtainPassword(request);

      if (username == null) {
         username = "";
      }

      if (password == null) {
         password = "";
      }

      username = username.trim();

      UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            username, password);

      // Allow subclasses to set the "details" property
      setDetails(request, authRequest);

      return this.getAuthenticationManager().authenticate(authRequest);
   }

}
```

所以，当我们访问http://localhost:8080/spring-boot-demo/hi，这样的请求时，会继续走后面的filter

###### 3.2.3 SessionManagementFilter

这个Filter会判断当前请求，是否存在会话。简单来说，就是request.getSession()，能不能拿到Session对象。

并不是说你拿不到就在这里把你拒了，这里只是设置一些属性和操作，真正决定的流程，不在这个Filter里

```java
public class SessionManagementFilter extends GenericFilterBean {
 	//一些属性

   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      if (request.getAttribute(FILTER_APPLIED) != null) {
         chain.doFilter(request, response);
         return;
      }

      request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

      if (!securityContextRepository.containsContext(request)) {//判断是否存在Session
         Authentication authentication = SecurityContextHolder.getContext()
               .getAuthentication();

         if (authentication != null && !trustResolver.isAnonymous(authentication)) {
            // The user has been authenticated during the current request, so call the
            // session strategy
            try {
               sessionAuthenticationStrategy.onAuthentication(authentication,
                     request, response);
            }
            catch (SessionAuthenticationException e) {
               // The session strategy can reject the authentication
               logger.debug(
                     "SessionAuthenticationStrategy rejected the authentication object",
                     e);
               SecurityContextHolder.clearContext();
               failureHandler.onAuthenticationFailure(request, response, e);

               return;
            }
            // Eagerly save the security context to make it available for any possible
            // re-entrant
            // requests which may occur before the current request completes.
            // SEC-1396.
            securityContextRepository.saveContext(SecurityContextHolder.getContext(),
                  request, response);
         }
         else {
            // No security context or authentication present. Check for a session
            // timeout
            if (request.getRequestedSessionId() != null
                  && !request.isRequestedSessionIdValid()) {
               if (logger.isDebugEnabled()) {
                  logger.debug("Requested session ID "
                        + request.getRequestedSessionId() + " is invalid.");
               }

               if (invalidSessionStrategy != null) {
                  invalidSessionStrategy
                        .onInvalidSessionDetected(request, response);
                  return;
               }
            }
         }
      }

      chain.doFilter(request, response);
   }

   //其它方法
}
```

###### 3.2.4 ExceptionTranslationFilter

这里并不直接干预校验问题，但这个Filter会捕获之后Filter执行时抛出来的异常，并在这里进行处理。在这里，决定了，发生异常后做了哪些事情

```java
public class ExceptionTranslationFilter extends GenericFilterBean {

  //一些属性和其它方法

   public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
         throws IOException, ServletException {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      try {
         chain.doFilter(request, response);

         logger.debug("Chain processed normally");
      }
      catch (IOException ex) {
         throw ex;
      }
      catch (Exception ex) {//这个异常才是关键所在
         // Try to extract a SpringSecurityException from the stacktrace
         Throwable[] causeChain = throwableAnalyzer.determineCauseChain(ex);
         RuntimeException ase = (AuthenticationException) throwableAnalyzer
               .getFirstThrowableOfType(AuthenticationException.class, causeChain);

         if (ase == null) {
            ase = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(
                  AccessDeniedException.class, causeChain);
         }

         if (ase != null) {
            if (response.isCommitted()) {
               throw new ServletException("Unable to handle the Spring Security Exception because the response is already committed.", ex);
            }
            handleSpringSecurityException(request, response, chain, ase);
         }
         else {
            // Rethrow ServletExceptions and RuntimeExceptions as-is
            if (ex instanceof ServletException) {
               throw (ServletException) ex;
            }
            else if (ex instanceof RuntimeException) {
               throw (RuntimeException) ex;
            }

            // Wrap other Exceptions. This shouldn't actually happen
            // as we've already covered all the possibilities for doFilter
            throw new RuntimeException(ex);
         }
      }
   }

		
  //其它方法


}
```

###### 3.2.5 FilterSecurityInterceptor

这个东西，名字上写着是拦截器，但这个拦截器，是Spring Security范畴的。实际上，他同时也是个Filter。

它最关键的是调用了AccessDecisionManager的decide方法，来决定放行与否

```java
public class AffirmativeBased extends AbstractAccessDecisionManager {

   public AffirmativeBased(List<AccessDecisionVoter<?>> decisionVoters) {
      super(decisionVoters);
   }

   // ~ Methods
   // ========================================================================================================

   /**
    * This concrete implementation simply polls all configured
    * {@link AccessDecisionVoter}s and grants access if any
    * <code>AccessDecisionVoter</code> voted affirmatively. Denies access only if there
    * was a deny vote AND no affirmative votes.
    * <p>
    * If every <code>AccessDecisionVoter</code> abstained from voting, the decision will
    * be based on the {@link #isAllowIfAllAbstainDecisions()} property (defaults to
    * false).
    * </p>
    *
    * @param authentication the caller invoking the method
    * @param object the secured object
    * @param configAttributes the configuration attributes associated with the method
    * being invoked
    *
    * @throws AccessDeniedException if access is denied
    */
   public void decide(Authentication authentication, Object object,
         Collection<ConfigAttribute> configAttributes) throws AccessDeniedException {
      int deny = 0;

      for (AccessDecisionVoter voter : getDecisionVoters()) {
         int result = voter.vote(authentication, object, configAttributes);

         if (logger.isDebugEnabled()) {
            logger.debug("Voter: " + voter + ", returned: " + result);
         }

         switch (result) {
         case AccessDecisionVoter.ACCESS_GRANTED:
            return;

         case AccessDecisionVoter.ACCESS_DENIED:
            deny++;

            break;

         default:
            break;
         }
      }

      if (deny > 0) {
         throw new AccessDeniedException(messages.getMessage(
               "AbstractAccessDecisionManager.accessDenied", "Access is denied"));
      }

      // To get this far, every AccessDecisionVoter abstained
      checkAllowIfAllAbstainDecisions();
   }
}
```

它这里会调用AccessDecisionVoter来判断是否放行

这个具体是检验算法，可以自行翻看，总之没有session的话，这里就会返回deny