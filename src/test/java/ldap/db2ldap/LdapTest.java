package ldap.db2ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.samples.useradmin.domain.JWUser;
import org.springframework.ldap.samples.useradmin.domain.User;
import org.springframework.ldap.samples.useradmin.service.UserService;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
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
    private LdapTemplate ldapTemplate;
    
    @Autowired
    private ContextSource contextSource;

    @Test
	public void createUser(){
    	JWUser user = new JWUser();
    	user.setId("cn=12312, ou=慧通事业部,ou=业务");
		user.setEmail("123@126.com");
		user.setEmployeeNumber("123");
		user.setLastName("lastName");
		user.setPhone("123");
		user.setTitle("title");
		user.setUid("ZH201703019");
		user.setUserPassword("c9c4c39a6ce3413ed32214ba89c1e777");
		userService.createJWUser(user);
//		ldapTemplate.create(user);
	}
    
    /**
     * 添加OU
     */
	@Test
	public void createNode(){
		Attributes attr = new BasicAttributes(); 
		BasicAttribute ocattr = new BasicAttribute("objectclass");
		ocattr.add("organizationalUnit");
		ocattr.add("top");
		attr.put(ocattr);
		
//		ldapTemplate.bind("ou=IT", null, attr);// buildDN() function
		ldapTemplate.bind("ou=业务", null, attr);
		ldapTemplate.bind("ou=慧通事业部, ou=业务", null, attr);
	}
	
    /**
     * 添加User
     */
	@Test
	public void createU(){
//		@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top", "shadowAccount" })
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
		ldapTemplate.bind("cn=jg2h1,ou=慧通事业部, ou=业务", null, attr);
	}
	
	
	@Test
	public void unbindOu(){
		ldapTemplate.unbind("cn=鞠光辉");
	}
	
	@Test
	public void search(){
		User user = userService.findUser("cn=Jane Doe,ou=General,ou=Accounting,ou=Departments");
		System.out.println(user.getEmail());
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
}
