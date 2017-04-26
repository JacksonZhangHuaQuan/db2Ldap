package ldap.db2ldap;

import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.samples.useradmin.domain.Group;
import org.springframework.ldap.samples.useradmin.domain.GroupRepo;
import org.springframework.ldap.samples.useradmin.domain.JWOrganization;
import org.springframework.ldap.samples.useradmin.domain.JWUser;
import org.springframework.ldap.samples.useradmin.service.OrganizationService;
import org.springframework.ldap.samples.useradmin.service.UserService;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:applicationContext.xml"
})
public class LdapTest {

	@Autowired
    private UserService userService;
	
	@Autowired
	private OrganizationService orgService;
    
    @Autowired
    private LdapTemplate ldapTemplate;
    
    @Autowired
    private ContextSource contextSource;
    
    @Autowired
    private GroupRepo groupRepo;
    
    /**
     * 测试新增一个用户，并将该用户添加到某个Group中
     */
    @Test
	public void createUser(){
    	JWUser user = new JWUser();
    	user.setId("cn=ZH201506006,ou=大数据平台研发工程师,ou=大数据平台部,ou=技术中心,ou=职能");
		user.setEmail("123@126.com");
		user.setEmployeeNumber("123");
		user.setLastName("lastName");
		user.setPhone("123");
		user.setTitle("title");
		user.setUid("ZH201703019");
		user.setUserPassword("c9c4c39a6ce3413ed32214ba89c1e777");
		
//		userService.createJWUser(user);
		addMemberToGroup(user);
//		ldapTemplate.create(user);
	}
    
    /**
     * 通过原生方式添加User
     */
	@Test
	public void createU(){
		Attributes attr = new BasicAttributes(); 
		BasicAttribute ocattr = new BasicAttribute("objectclass");
		ocattr.add("top");
		ocattr.add("organizationalPerson");
		ocattr.add("shadowAccount");
		attr.put(ocattr);
		attr.put("userPassword", "12");
		attr.put("sn", "12");
		attr.put("uid", "12");
		
//		ldapTemplate.bind("ou=IT", null, attr);// buildDN() function
		ldapTemplate.bind("cn=jg2h1,ou=项目组,ou=事业部, ou=业务", null, attr);
	}
    
    /**
     * 通过Entity注解Java类的方式，增加一个组织机构，两种方式，一个通过orgService接口，另一个中直接通过ldapTemplate
     */
    @Test
	public void createOrganization(){
    	JWOrganization org = new JWOrganization();
    	org.setId("ou=1, ou=慧通事业部, ou=业务");
    	orgService.createJWOrg(org);
//		ldapTemplate.create(org);
	}
    
    /**
     * 通过Attributes方式增加一个组织结构
     */
	@Test
	public void createNode(){
		Attributes attr = new BasicAttributes(); 
		BasicAttribute ocattr = new BasicAttribute("objectclass");
		ocattr.add("organizationalUnit");
		ocattr.add("top");
		attr.put(ocattr);
		ldapTemplate.bind("ou=业务", null, attr);
		ldapTemplate.bind("ou=事业部, ou=业务", null, attr);
		ldapTemplate.bind("ou=项目组,ou=事业部, ou=业务", null, attr);
	}
	
	/**
	 * 添加一个Group,并向该Group中增加一个member
	 */
	@Test
	public void createGroup(){
		Attributes attr = new BasicAttributes(); 
		BasicAttribute ocattr = new BasicAttribute("objectclass");
		ocattr.add("groupOfNames");
		ocattr.add("top");
		attr.put(ocattr);
		attr.put("member", "cn=ZH201506003,ou=大数据平台研发工程师,ou=大数据平台部,ou=技术中心,ou=职能,dc=openldap,dc=jw,dc=cn");
		ldapTemplate.bind("cn=AUTHORITY_SYSTEM_USER, ou=Group", null, attr);
	}
	
	
    /**
     * 向Group中添加member
     */
	public void addMemberToGroup(JWUser savedUser){
	    Group userGroup = groupRepo.findByName(GroupRepo.USER_GROUP);
	    LdapName ldapName = LdapNameBuilder.newInstance("dc=openldap,dc=jw,dc=cn").add(savedUser.getId()).build();
	    // The DN the member attribute must be absolute
//	    userGroup.addMember(LdapUtils.newLdapName(savedUser.getId()));
	    userGroup.addMember(ldapName);
	    groupRepo.save(userGroup);
	}
	
	@Test
	public void unbindOu(){
		ldapTemplate.unbind("cn=AUTHORITY_SYSTEM_USER,ou=Group");
	}
	
	/**
	 * clear ldap 
	 */
	@Test
	public void clear(){
		try {
			LdapTestUtils.clearSubContexts(contextSource, LdapUtils.emptyLdapName());
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void getMemberFromGroup(){
		String groupSearchBase = "ou=Group";
		DefaultLdapAuthoritiesPopulator p = new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
		Set<GrantedAuthority> set = p.getGroupMembershipRoles("cn=ZH201506006,ou=大数据平台研发工程师,ou=大数据平台部,ou=技术中心,ou=职能", "ZH201506006");
		System.out.println(set.size());
	} 
}
