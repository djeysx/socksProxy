package sp.filter;

import org.junit.Test;

import junit.framework.Assert;

public class DomainRegistryTest {

    DomainRegistry domainRegistry = new DomainRegistryOptimized("src/test/resources/rejectedDomain.txt");
    // DomainRegistry domainRegistry = new DomainRegistryImpl("src/test/resources/rejectedDomain.txt");

    @Test
    public void test_equals() {
        Assert.assertTrue(domainRegistry.match("domainA.fr"));
        Assert.assertTrue(domainRegistry.match("domainA.com"));
        Assert.assertTrue(domainRegistry.match("subdomainB.domainB.net"));
        Assert.assertTrue(domainRegistry.match("subdomainB2.domainB.net"));
    }

    @Test
    public void test_subdomain() {
        Assert.assertTrue(domainRegistry.match("test.domainA.fr"));
        Assert.assertTrue(domainRegistry.match("test.domainA.com"));
        Assert.assertTrue(domainRegistry.match("test.subdomainB.domainB.net"));
        Assert.assertTrue(domainRegistry.match("test.subdomainB2.domainB.net"));
        Assert.assertTrue(domainRegistry.match("test2.test1.subdomainB2.domainB.net"));
    }

    @Test
    public void test_not_equals() {
        // exists level 0
        Assert.assertFalse(domainRegistry.match("domainX.nowhere"));
        // exists level 1
        Assert.assertFalse(domainRegistry.match("domainX.fr"));
        // exists level 1
        Assert.assertFalse(domainRegistry.match("domainX.com"));
        // exists level 2
        Assert.assertFalse(domainRegistry.match("subdomainX.domainB.net"));
        // exists level 2
        Assert.assertFalse(domainRegistry.match("subdomainX2.domainB.net"));
        // exists level 2
        Assert.assertFalse(domainRegistry.match("domainB.net"));
    }

    @Test
    public void test_z_perf() {
        for (int i = 0; i < 10000; i++) {
            test_equals();
            test_subdomain();
            test_not_equals();
        }
    }

}
