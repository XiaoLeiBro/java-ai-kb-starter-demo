/**
 * 用户与权限限界上下文（User Bounded Context）.
 *
 * <p>Demo 版本只做最小集：
 *
 * <ul>
 *   <li><b>User</b>（聚合根）：登录用户
 *   <li><b>Role</b>（枚举）：ADMIN / USER
 * </ul>
 *
 * <p>商业版扩展方向（不在 Demo 范围）：
 *
 * <ul>
 *   <li>组织架构（Organization / Department）
 *   <li>细粒度 RBAC（Permission / Resource）
 *   <li>多租户隔离
 *   <li>企业 SSO（OAuth2 / LDAP）
 * </ul>
 */
package com.brolei.aikb.domain.user;
