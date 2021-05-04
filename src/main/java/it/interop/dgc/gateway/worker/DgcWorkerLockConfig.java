/*-
 *   Copyright (C) 2021 Presidenza del Consiglio dei Ministri.
 *   Please refer to the AUTHORS file for more information. 
 *   This program is free software: you can redistribute it and/or modify 
 *   it under the terms of the GNU Affero General Public License as 
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 *   GNU Affero General Public License for more details.
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.   
 */
package it.interop.dgc.gateway.worker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@Configuration
//defaultLockAtMostFor specifies the default time that the lock should be retained at the end of the execution node, using the ISO8601 Duration format
//The effect is that when the locked node is hung up, the lock cannot be released, causing other nodes to be unable to perform the next task
//The default here is 20M
//About the ISO8601 Duration format is not available, you can check the relevant information on the Internet. It should be a set of specifications that stipulate some time expressions
@EnableSchedulerLock(defaultLockAtMostFor = "${dgc.worker.defaultLockAtMostFor}")
public class DgcWorkerLockConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        //Environmental variables-Different environments need to be distinguished to avoid conflicts, such as dev environment and test environment. When both are deployed, only one instance will be used. At this time, the related environment will not start
        return new RedisLockProvider(connectionFactory);
    }
}