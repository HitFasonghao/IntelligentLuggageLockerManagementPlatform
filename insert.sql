-- =============================================
-- 固定数据（系统初始化必须）
-- 以下四张表的主键必须显式指定，因为存在外键引用关系
-- =============================================

-- 审核流程节点
INSERT INTO audit_nodes (audit_node_id, name, type, `order`, auto_pass, timeout_hours, is_active) VALUES
(1, '资质审核', 'qualification', 1, FALSE, 48, TRUE),
(2, '功能测试', 'functional_test', 2, FALSE, 72, TRUE),
(3, '性能测试', 'performance', 3, FALSE, 72, TRUE),
(4, '合规检查', 'compliance', 4, FALSE, 24, FALSE),
(5, '人工审核', 'manual_review', 5, FALSE, 24, TRUE);

-- 角色
INSERT INTO pc_role (role_id, name, code, description) VALUES
(1, '超级管理员', 'SUPER_ADMIN', NULL),
(2, '普通管理员', 'REGULAR_ADMIN', '平台管理员，不可创建管理员账号'),
(3, '厂商用户', 'VENDOR_USER', '未绑定厂商的厂商用户'),
(4, '厂商主管理员', 'MAIN_VENDOR_USER', '厂商主管理员，可管理厂商用户'),
(5, '厂商普通管理员', 'REGULAR_VENDOR_USER', '厂商普通用户');

-- 权限（菜单）
-- parent_id 引用的是本表的 permission_id
INSERT INTO pc_permission (permission_id, name, code, type, parent_id, path, redirect, icon, component, layout, keep_alive, method, description, show_status, enable_status, sort) VALUES
(1,  '系统管理',       'SysMgt',              'MENU', NULL, NULL,                              NULL, 'i-fe:grid',           NULL,                                              NULL, NULL, NULL, NULL, 1, 1, 2),
(2,  '资源管理',       'Resource_Mgt',        'MENU', 1,    '/pms/resource',                   NULL, 'i-fe:list',           '/src/views/pms/resource/index.vue',               NULL, NULL, NULL, NULL, 1, 1, 1),
(3,  '个人资料',       'UserProfile',         'MENU', NULL, '/profile',                        NULL, 'i-fe:user',           '/src/views/profile/admin/index.vue',              NULL, NULL, NULL, NULL, 0, 1, 100),
(4,  '基础功能',       'Base',                'MENU', NULL, '',                                NULL, 'i-fe:grid',           NULL,                                              '',   NULL, NULL, NULL, 1, 1, 0),
(5,  '基础组件',       'BaseComponents',      'MENU', 4,    '/base/components',                NULL, 'i-me:awesome',        '/src/views/base/index.vue',                       NULL, NULL, NULL, NULL, 1, 1, 1),
(6,  'Unocss',         'Unocss',              'MENU', 4,    '/base/unocss',                    NULL, 'i-me:awesome',        '/src/views/base/unocss.vue',                      NULL, NULL, NULL, NULL, 1, 1, 2),
(7,  'KeepAlive',      'KeepAlive',           'MENU', 4,    '/base/keep-alive',                NULL, 'i-me:awesome',        '/src/views/base/keep-alive.vue',                  NULL, 1,    NULL, NULL, 1, 1, 3),
(8,  '图标 Icon',      'Icon',                'MENU', 4,    '/base/icon',                      NULL, 'i-fe:feather',        '/src/views/base/unocss-icon.vue',                 '',   NULL, NULL, NULL, 1, 1, 0),
(9,  'MeModal',        'TestModal',           'MENU', 4,    '/testModal',                      NULL, 'i-me:dialog',         '/src/views/base/test-modal.vue',                  NULL, NULL, NULL, NULL, 1, 1, 5),
(10, '厂商审核管理',   'AuditMgt',            'MENU', NULL, NULL,                              NULL, 'i-fe:clipboard',      NULL,                                              NULL, NULL, NULL, NULL, 1, 1, 3),
(11, '审核列表',       'AuditVendors',        'MENU', 10,   '/audit/vendors',                  NULL, 'i-fe:file-text',      '/src/views/audit/vendors/index.vue',              NULL, NULL, NULL, NULL, 1, 1, 1),
(12, '审核详情',       'AuditReview',         'MENU', 10,   '/audit/review/:id',               NULL, 'i-fe:file-text',      '/src/views/audit/review/index.vue',               NULL, NULL, NULL, NULL, 0, 1, 2),
(13, '审核任务工作台', 'AuditTasks',          'MENU', 10,   '/audit/tasks',                    NULL, 'i-fe:check-square',   '/src/views/audit/tasks/index.vue',                NULL, NULL, NULL, NULL, 1, 1, 3),
(14, '资质审核',       'VendorMgt',           'MENU', NULL, NULL,                              NULL, 'i-fe:briefcase',      NULL,                                              NULL, NULL, NULL, NULL, 1, 1, 5),
(15, '入驻申请',       'VendorApplication',   'MENU', 14,   '/vendor/application',             NULL, 'i-fe:edit-3',         '/src/views/vendor/application/index.vue',         NULL, NULL, NULL, NULL, 1, 1, 1),
(16, '审核记录',       'VendorProgress',      'MENU', 14,   '/vendor/progress',                NULL, 'i-fe:activity',       '/src/views/vendor/progress/index.vue',            NULL, NULL, NULL, NULL, 1, 1, 2),
(17, '资质',           'VendorInfo',          'MENU', 14,   '/vendor/info',                    NULL, 'i-fe:info',           '/src/views/vendor/info/index.vue',                NULL, NULL, NULL, NULL, 1, 1, 3),
(18, '用户管理',       'VendorUserMgt',       'MENU', 30,   '/vendor/users',                   NULL, 'i-fe:users',          '/src/views/vendor/users/index.vue',               NULL, NULL, NULL, NULL, 1, 1, 2),
(19, '审核任务分配',   'AuditAssignment',     'MENU', 10,   '/audit/assignments',              NULL, 'i-fe:share-2',        '/src/views/audit/assignments/index.vue',          NULL, NULL, NULL, NULL, 1, 1, 0),
(20, '厂商管理',       'VendorMgmt',          'MENU', NULL, NULL,                              NULL, 'i-fe:briefcase',      NULL,                                              NULL, NULL, NULL, NULL, 1, 1, 4),
(21, '正常厂商管理',   'VendorMgmtApproved',  'MENU', 20,   '/vendor-mgmt/approved',           NULL, 'i-fe:check-circle',   '/src/views/vendor-mgmt/approved/index.vue',       NULL, NULL, NULL, NULL, 1, 1, 0),
(22, '异常厂商管理',   'VendorMgmtAbnormal',  'MENU', 20,   '/vendor-mgmt/abnormal',           NULL, 'i-fe:alert-triangle', '/src/views/vendor-mgmt/abnormal/index.vue',       NULL, NULL, NULL, NULL, 1, 1, 1),
(23, '任务处理',       'AuditTaskHandle',     'MENU', 10,   '/audit/tasks/handle/:taskId',     NULL, 'i-fe:edit',           '/src/views/audit/tasks/handle/index.vue',         NULL, 0,    NULL, NULL, 0, 1, 4),
(24, '审核记录',       'AuditRecords',        'MENU', 10,   '/audit/records',                  NULL, 'i-fe:file-text',      '/src/views/audit/records/index.vue',              NULL, 0,    NULL, NULL, 1, 1, 5),
(25, '设备管理',       'DeviceMgt',           'MENU', NULL, NULL,                              NULL, 'i-fe:hard-drive',     NULL,                                              NULL, NULL, NULL, NULL, 1, 1, 7),
(26, '寄存柜管理',     'DeviceCabinet',       'MENU', 25,   '/device/cabinet',                 NULL, 'i-fe:box',            '/src/views/device/cabinet/index.vue',             NULL, 1,    NULL, NULL, 1, 1, 0),
(27, '寄存柜种类管理', 'DeviceCabinetKind',   'MENU', 25,   '/device/cabinet-kind',            NULL, 'i-fe:layers',         '/src/views/device/cabinet-kind/index.vue',        NULL, 1,    NULL, NULL, 1, 1, 1),
(28, '柜群管理',       'DeviceCluster',       'MENU', 25,   '/device/cluster',                 NULL, 'i-fe:grid',           '/src/views/device/cluster/index.vue',             NULL, 1,    NULL, NULL, 1, 1, 2),
(29, '柜群详情',       'DeviceClusterDetail', 'MENU', 25,   '/device/cluster/detail/:clusterId', NULL, 'i-fe:info',         '/src/views/device/cluster/detail.vue',            NULL, 0,    NULL, NULL, 0, 1, 3),
(30, '厂商管理',       'VendorSideMgmt',      'MENU', NULL, NULL,                              NULL, 'i-fe:settings',       NULL,                                              NULL, NULL, NULL, NULL, 1, 1, 6),
(31, '信息管理',       'VendorVendorInfo',    'MENU', 30,   '/vendor/vendor-info',             NULL, 'i-fe:info',           '/src/views/vendor/vendor-info/index.vue',         NULL, NULL, NULL, NULL, 1, 1, 1),
(32, '个人资料',       'VendorUserProfile',   'MENU', NULL, '/profile',                        NULL, 'i-fe:user',           '/src/views/profile/vendor/index.vue',             NULL, NULL, NULL, NULL, 0, 1, 100);

-- 角色-权限关联（不指定主键，自增即可）
INSERT INTO pc_role_permission (role_id, permission_id) VALUES
-- 超级管理员
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(1, 10), (1, 11), (1, 12), (1, 13), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24),
-- 普通管理员
(2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
(2, 10), (2, 11), (2, 12), (2, 13), (2, 19), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24),
-- 厂商用户（未绑定厂商）
(3, 14), (3, 15), (3, 16), (3, 17), (3, 32),
-- 厂商主管理员
(4, 14), (4, 15), (4, 16), (4, 17), (4, 18),
(4, 25), (4, 26), (4, 27), (4, 28), (4, 29), (4, 30), (4, 31), (4, 32),
-- 厂商普通管理员
(5, 14), (5, 15), (5, 16), (5, 17),
(5, 25), (5, 26), (5, 27), (5, 28), (5, 29), (5, 32);

-- =============================================
-- 样本数据（演示/测试用，不指定主键）
-- 注意：以下数据假设插入到空库中，自增 ID 从 1 开始
-- 各表之间的外键引用依赖于插入顺序产生的自增 ID
-- =============================================

-- 平台管理员（3条：1超管 + 2普通，自增 ID → 1,2,3）
INSERT INTO platform_admins (username, password, real_name, email, phone, is_super_admin, is_active) VALUES
('admin0',  '888888', '张超',   'admin0@example.com',  '19200000001', TRUE,  TRUE),
('admin1',  '888888', '李明',   'admin1@example.com',  '19200000002', FALSE, TRUE),
('admin2',  '888888', '王芳',   'admin2@example.com',  '19200000003', FALSE, TRUE);

-- 厂商用户（4条，自增 ID → 1,2,3,4）
INSERT INTO vendor_users (username, password, email, phone, real_name, status) VALUES
('vendor1', '666666', 'vendor1@example.com', '18700000001', '赵一', 'active'),
('vendor2', '666666', 'vendor2@example.com', '18700000002', '钱二', 'active'),
('vendor3', '666666', 'vendor3@example.com', '18700000003', '孙三', 'active'),
('vendor4', '666666', 'vendor4@example.com', '18700000004', '周四', 'active');

-- 厂商（6条，自增 ID → 1~6）
INSERT INTO vendors (company_name, short_name, license_no, legal_person, legal_person_id,
                     contact_person, contact_phone, contact_email, company_address, website,
                     introduction, business_scope,
                     api_endpoint, vendor_access_token, platform_access_token,
                     status, submitted_time, approved_time, effective_from, effective_to, admin_id) VALUES
-- 1: 已通过
('智行科技有限公司', '智行科技', '91110108MA01ABCD1X', '赵一', '110101199001011234',
 '赵一', '18700000001', 'vendor1@example.com', '北京市海淀区中关村大街1号', 'https://www.zhixing-tech.com',
 '专注于智能存储设备研发与运营的高新技术企业', '智能寄存柜研发、销售、运营；物联网技术服务',
 'http://localhost:9001', 'storage-system-token-2026', 'PAT-sample-token-001',
 'approved', '2026-03-01 10:00:00', '2026-03-05 16:00:00', '2026-03-05 16:00:00', '2027-03-05 16:00:00', 1),
-- 2: 功能测试阶段
('安存科技有限公司', '安存科技', '91310115MA1K2BCD3E', '孙三', '310115199205054321',
 '孙三', '18700000003', 'vendor3@example.com', '上海市浦东新区张江路88号', 'https://www.ancun-tech.com',
 '致力于安全存储解决方案的科技公司', '智能储物柜生产、安装、维护；安防技术咨询',
 'http://localhost:9002', 'ancun-access-token-2026', NULL,
 'testing', '2026-03-10 09:00:00', NULL, NULL, NULL, NULL),
-- 3: 已通过
('锐盒科技有限公司', '锐盒科技', '91440300MA5F3DEF7G', '赵一', '110101199001011234',
 '赵一', '18700000001', 'ruihe@example.com', '深圳市南山区科技园南路22号', 'https://www.ruihe-tech.com',
 '国内领先的智能储物柜整体解决方案提供商', '智能柜体研发制造；嵌入式软件开发；物联网平台运营',
 'http://localhost:9003', 'ruihe-access-token-2026', 'PAT-sample-token-003',
 'approved', '2026-02-20 08:30:00', '2026-02-26 17:00:00', '2026-02-26 17:00:00', '2027-02-26 17:00:00', 1),
-- 4: 待资质审核
('云柜科技有限公司', '云柜科技', '91510100MA6C8GHI9J', '周四', '510100199308087654',
 '周四', '18700000004', 'vendor4@example.com', '成都市高新区天府大道666号', 'https://www.yungui-tech.com',
 '云端智能柜管理平台服务商', '云计算服务；智能硬件销售；系统集成',
 'http://localhost:9004', 'yungui-access-token-2026', NULL,
 'pending', '2026-03-12 14:00:00', NULL, NULL, NULL, NULL),
-- 5: 已驳回（资质审核失败后重新提交，又在待资质审核）
('易存科技有限公司', '易存科技', '91320100MA1NJKLM2N', '周四', '510100199308087654',
 '周四', '18700000004', 'yicun@example.com', '南京市玄武区珠江路88号', NULL,
 '提供便捷的行李寄存自助服务', '寄存服务；自助设备租赁',
 NULL, NULL, NULL,
 'pending', '2026-03-08 10:00:00', NULL, NULL, NULL, NULL),
-- 6: 性能测试阶段
('博联智能科技有限公司', '博联智能', '91330100MA2BOPQR3S', '孙三', '310115199205054321',
 '孙三', '18700000003', 'bolian@example.com', '杭州市余杭区文一西路998号', 'https://www.bolian-smart.com',
 '专注于人工智能与物联网融合的智能存储企业', 'AI算法研发；智能柜体制造；大数据分析服务',
 'http://localhost:9006', 'bolian-access-token-2026', NULL,
 'testing', '2026-03-06 11:00:00', NULL, NULL, NULL, NULL);

-- 厂商用户关联
-- 用户1(赵一)→厂商1主管、厂商3主管；用户2(钱二)→厂商1普通
-- 用户3(孙三)→厂商2主管、厂商6主管；用户4(周四)→厂商4主管、厂商5主管
INSERT INTO vendor_user_relation (vendor_user_id, vendor_id, is_main) VALUES
(1, 1, TRUE),
(2, 1, FALSE),
(1, 3, TRUE),
(3, 2, TRUE),
(3, 6, TRUE),
(4, 4, TRUE),
(4, 5, TRUE);

-- 审核记录（7条，自增 ID → 1~7）
INSERT INTO vendor_audit_records (vendor_id, vendor_user_id, round, type, data, admin_id, created_time, completed_time, result) VALUES
-- 记录1: 厂商1 第1轮 已通过
(1, 1, 1, 'initial',
 '{"companyName":"智行科技有限公司","shortName":"智行科技","licenseNo":"91110108MA01ABCD1X","legalPerson":"赵一","legalPersonId":"110101199001011234","contactPerson":"赵一","contactPhone":"18700000001","contactEmail":"vendor1@example.com","companyAddress":"北京市海淀区中关村大街1号","website":"https://www.zhixing-tech.com","introduction":"专注于智能存储设备研发与运营的高新技术企业","businessScope":"智能寄存柜研发、销售、运营；物联网技术服务","apiEndpoint":"http://localhost:9001","vendorAccessToken":"storage-system-token-2026"}',
 1, '2026-03-01 10:00:00', '2026-03-05 16:00:00', 'approved'),
-- 记录2: 厂商2 第1轮 功能测试阶段
(2, 3, 1, 'initial',
 '{"companyName":"安存科技有限公司","shortName":"安存科技","licenseNo":"91310115MA1K2BCD3E","legalPerson":"孙三","legalPersonId":"310115199205054321","contactPerson":"孙三","contactPhone":"18700000003","contactEmail":"vendor3@example.com","companyAddress":"上海市浦东新区张江路88号","website":"https://www.ancun-tech.com","introduction":"致力于安全存储解决方案的科技公司","businessScope":"智能储物柜生产、安装、维护；安防技术咨询","apiEndpoint":"http://localhost:9002","vendorAccessToken":"ancun-access-token-2026"}',
 2, '2026-03-10 09:00:00', NULL, 'pending_functional_test'),
-- 记录3: 厂商3 第1轮 已通过
(3, 1, 1, 'initial',
 '{"companyName":"锐盒科技有限公司","shortName":"锐盒科技","licenseNo":"91440300MA5F3DEF7G","legalPerson":"赵一","legalPersonId":"110101199001011234","contactPerson":"赵一","contactPhone":"18700000001","contactEmail":"ruihe@example.com","companyAddress":"深圳市南山区科技园南路22号","website":"https://www.ruihe-tech.com","introduction":"国内领先的智能储物柜整体解决方案提供商","businessScope":"智能柜体研发制造；嵌入式软件开发；物联网平台运营","apiEndpoint":"http://localhost:9003","vendorAccessToken":"ruihe-access-token-2026"}',
 1, '2026-02-20 08:30:00', '2026-02-26 17:00:00', 'approved'),
-- 记录4: 厂商4 第1轮 待资质审核
(4, 4, 1, 'initial',
 '{"companyName":"云柜科技有限公司","shortName":"云柜科技","licenseNo":"91510100MA6C8GHI9J","legalPerson":"周四","legalPersonId":"510100199308087654","contactPerson":"周四","contactPhone":"18700000004","contactEmail":"vendor4@example.com","companyAddress":"成都市高新区天府大道666号","website":"https://www.yungui-tech.com","introduction":"云端智能柜管理平台服务商","businessScope":"云计算服务；智能硬件销售；系统集成","apiEndpoint":"http://localhost:9004","vendorAccessToken":"yungui-access-token-2026"}',
 0, '2026-03-12 14:00:00', NULL, 'pending_qualification'),
-- 记录5: 厂商5 第1轮 资质审核失败
(5, 4, 1, 'initial',
 '{"companyName":"易存科技有限公司","shortName":"易存科技","licenseNo":"91320100MA1NJKLM2N","legalPerson":"周四","legalPersonId":"510100199308087654","contactPerson":"周四","contactPhone":"18700000004","contactEmail":"yicun@example.com","companyAddress":"南京市玄武区珠江路88号","introduction":"提供便捷的行李寄存自助服务","businessScope":"寄存服务；自助设备租赁"}',
 3, '2026-03-08 10:00:00', '2026-03-09 15:00:00', 'qualification_failed'),
-- 记录6: 厂商5 第2轮 重新提交，待资质审核
(5, 4, 2, 'initial',
 '{"companyName":"易存科技有限公司","shortName":"易存科技","licenseNo":"91320100MA1NJKLM2N","legalPerson":"周四","legalPersonId":"510100199308087654","contactPerson":"周四","contactPhone":"18700000004","contactEmail":"yicun@example.com","companyAddress":"南京市玄武区珠江路88号","introduction":"提供便捷的行李寄存自助服务","businessScope":"寄存服务；自助设备租赁"}',
 0, '2026-03-11 09:00:00', NULL, 'pending_qualification'),
-- 记录7: 厂商6 第1轮 性能测试阶段
(6, 3, 1, 'initial',
 '{"companyName":"博联智能科技有限公司","shortName":"博联智能","licenseNo":"91330100MA2BOPQR3S","legalPerson":"孙三","legalPersonId":"310115199205054321","contactPerson":"孙三","contactPhone":"18700000003","contactEmail":"bolian@example.com","companyAddress":"杭州市余杭区文一西路998号","website":"https://www.bolian-smart.com","introduction":"专注于人工智能与物联网融合的智能存储企业","businessScope":"AI算法研发；智能柜体制造；大数据分析服务","apiEndpoint":"http://localhost:9006","vendorAccessToken":"bolian-access-token-2026"}',
 2, '2026-03-06 11:00:00', NULL, 'pending_performance_test');

-- 审核任务
INSERT INTO audit_tasks (vendor_id, audit_record_id, audit_node_id, admin_id, status, priority, due_date, completed_time, notes, passed, created_time) VALUES
-- 厂商1（记录1）：4个节点全部完成通过
(1, 1, 1, 1, 'completed', 'medium', '2026-03-03 10:00:00', '2026-03-02 14:30:00', '资质材料齐全，审核通过',                  TRUE,  '2026-03-01 10:00:00'),
(1, 1, 2, 2, 'completed', 'medium', '2026-03-05 10:00:00', '2026-03-03 17:00:00', 'API接口响应正常，功能测试通过',            TRUE,  '2026-03-02 14:30:00'),
(1, 1, 3, 2, 'completed', 'medium', '2026-03-07 10:00:00', '2026-03-04 15:00:00', '性能指标达标，P99延迟<200ms',              TRUE,  '2026-03-03 17:00:00'),
(1, 1, 5, 1, 'completed', 'high',   '2026-03-08 10:00:00', '2026-03-05 16:00:00', '综合评估合格，同意入驻',                   TRUE,  '2026-03-04 15:00:00'),
-- 厂商2（记录2）：资质已通过，功能测试待分配
(2, 2, 1, 2, 'completed', 'medium', '2026-03-12 09:00:00', '2026-03-11 11:00:00', '资质材料审核通过',                         TRUE,  '2026-03-10 09:00:00'),
(2, 2, 2, NULL, 'pending', NULL,    NULL,                   NULL,                   NULL,                                       NULL,  '2026-03-11 11:00:00'),
-- 厂商3（记录3）：4个节点全部完成通过
(3, 3, 1, 3, 'completed', 'high',   '2026-02-22 08:30:00', '2026-02-21 16:00:00', '材料完整，营业执照有效期内',               TRUE,  '2026-02-20 08:30:00'),
(3, 3, 2, 2, 'completed', 'high',   '2026-02-24 08:30:00', '2026-02-23 11:00:00', '全部接口测试通过，兼容性良好',             TRUE,  '2026-02-21 16:00:00'),
(3, 3, 3, 2, 'completed', 'high',   '2026-02-26 08:30:00', '2026-02-25 14:30:00', 'QPS达到预期，压测无异常',                  TRUE,  '2026-02-23 11:00:00'),
(3, 3, 5, 1, 'completed', 'urgent', '2026-02-27 08:30:00', '2026-02-26 17:00:00', '各项指标优秀，批准入驻',                   TRUE,  '2026-02-25 14:30:00'),
-- 厂商4（记录4）：待资质审核（待分配）
(4, 4, 1, NULL, 'pending', NULL,    NULL,                   NULL,                   NULL,                                       NULL,  '2026-03-12 14:00:00'),
-- 厂商5（记录5）：第1轮资质审核失败
(5, 5, 1, 3, 'completed', 'low',    '2026-03-10 10:00:00', '2026-03-09 15:00:00', '营业执照照片模糊，经营范围与申报不符',     FALSE, '2026-03-08 10:00:00'),
-- 厂商5（记录6）：第2轮重新提交，待资质审核（待分配）
(5, 6, 1, NULL, 'pending', NULL,    NULL,                   NULL,                   NULL,                                       NULL,  '2026-03-11 09:00:00'),
-- 厂商6（记录7）：资质、功能测试已通过，性能测试进行中
(6, 7, 1, 3, 'completed', 'medium', '2026-03-08 11:00:00', '2026-03-07 16:00:00', '资质审核通过',                             TRUE,  '2026-03-06 11:00:00'),
(6, 7, 2, 2, 'completed', 'medium', '2026-03-10 11:00:00', '2026-03-09 18:00:00', '功能测试通过，接口规范',                   TRUE,  '2026-03-07 16:00:00'),
(6, 7, 3, 2, 'pending',   'high',   '2026-03-14 11:00:00', NULL,                   NULL,                                       NULL,  '2026-03-09 18:00:00');

-- 寄存柜种类（自增 ID → 1~8）
-- 厂商1: ID 1~3, 厂商3: ID 4~8
INSERT INTO cabinet_kinds (vendor_id, name, description, charge, time_unit) VALUES
(1, '小型柜', '适合存放背包、手提袋等小件物品',          2.00, 'anHour'),
(1, '中型柜', '适合存放行李箱（20寸以内）',              3.00, 'anHour'),
(1, '大型柜', '适合存放大号行李箱（28寸以内）',          5.00, 'anHour'),
(3, '迷你柜', '适合存放钱包、手机等随身小物件',          1.00, 'halfAnHour'),
(3, '标准柜', '适合存放双肩包、手提箱等中等物品',        2.50, 'halfAnHour'),
(3, '加大柜', '适合存放大型行李箱',                      4.00, 'halfAnHour'),
(3, '超大柜', '适合存放多件行李或大型物品',              6.00, 'anHour'),
(3, '临时柜', '10分钟快存快取，适合短暂寄存',            0.50, 'tenMinutes');

-- 柜群（自增 ID → 1~7）
-- 厂商1: ID 1~3, 厂商3: ID 4~7
INSERT INTO clusters (vendor_id, name, location, longitude, dimension, status, description, created_time, updated_time) VALUES
(1, '北京南站A区', '北京南站一层东侧出口',           116.378500, 39.865200, 'using',     '靠近东侧出口，人流量大',       '2026-03-06 10:00:00', '2026-03-06 10:00:00'),
(1, '朝阳大悦城',  '朝阳大悦城负一层入口',           116.473100, 39.921800, 'using',     '商场负一层主入口旁',           '2026-03-06 11:00:00', '2026-03-06 11:00:00'),
(1, '首都机场T3',  '首都机场T3航站楼到达层',         116.603100, 40.078400, 'using',     '国际到达出口左侧',             '2026-03-07 09:00:00', '2026-03-07 09:00:00'),
(3, '深圳北站',    '深圳北站东广场一层',             114.029300, 22.609700, 'using',     '高铁到达层东侧',               '2026-02-28 10:00:00', '2026-02-28 10:00:00'),
(3, '万象城',      '深圳万象城L1层南门',             114.069800, 22.536500, 'using',     '靠近南门扶梯旁',               '2026-02-28 11:00:00', '2026-02-28 11:00:00'),
(3, '世界之窗',    '世界之窗景区入口广场',           113.974000, 22.534300, 'using',     '景区入口售票处旁',             '2026-03-01 09:00:00', '2026-03-01 09:00:00'),
(3, '宝安机场T3',  '深圳宝安国际机场T3航站楼出发层', 113.818000, 22.639400, 'forbidden', '设备维护中，暂停使用',         '2026-03-01 14:00:00', '2026-03-10 08:00:00');

-- 寄存柜
INSERT INTO cabinets (vendor_id, device_id, number, status, kind_id, cluster_id) VALUES
-- === 厂商1 ===
-- 北京南站A区（柜群1）：10台
(1, 'ZX-A001', 1,  'free',    1, 1),
(1, 'ZX-A002', 2,  'free',    1, 1),
(1, 'ZX-A003', 3,  'free',    1, 1),
(1, 'ZX-A004', 4,  'free',    1, 1),
(1, 'ZX-A005', 5,  'free',    2, 1),
(1, 'ZX-A006', 6,  'free',    2, 1),
(1, 'ZX-A007', 7,  'free',    2, 1),
(1, 'ZX-A008', 8,  'using',   3, 1),
(1, 'ZX-A009', 9,  'using',   3, 1),
(1, 'ZX-A010', 10, 'free',    3, 1),
-- 朝阳大悦城（柜群2）：8台
(1, 'ZX-B001', 1, 'free',     1, 2),
(1, 'ZX-B002', 2, 'free',     1, 2),
(1, 'ZX-B003', 3, 'using',    1, 2),
(1, 'ZX-B004', 4, 'free',     2, 2),
(1, 'ZX-B005', 5, 'free',     2, 2),
(1, 'ZX-B006', 6, 'free',     2, 2),
(1, 'ZX-B007', 7, 'free',     3, 2),
(1, 'ZX-B008', 8, 'free',     3, 2),
-- 首都机场T3（柜群3）：8台
(1, 'ZX-C001', 1, 'free',     1, 3),
(1, 'ZX-C002', 2, 'free',     1, 3),
(1, 'ZX-C003', 3, 'free',     2, 3),
(1, 'ZX-C004', 4, 'free',     2, 3),
(1, 'ZX-C005', 5, 'using',    3, 3),
(1, 'ZX-C006', 6, 'free',     3, 3),
(1, 'ZX-C007', 7, 'free',     3, 3),
(1, 'ZX-C008', 8, 'free',     3, 3),
-- 厂商1 未分配：6台
(1, 'ZX-X001', NULL, 'free', NULL, NULL),
(1, 'ZX-X002', NULL, 'free', NULL, NULL),
(1, 'ZX-X003', NULL, 'free', NULL, NULL),
(1, 'ZX-X004', NULL, 'free', NULL, NULL),
(1, 'ZX-X005', NULL, 'free', NULL, NULL),
(1, 'ZX-X006', NULL, 'free', NULL, NULL),
-- === 厂商3 ===
-- 深圳北站（柜群4）：12台
(3, 'RH-A001', 1,  'free',    4, 4),
(3, 'RH-A002', 2,  'free',    4, 4),
(3, 'RH-A003', 3,  'free',    4, 4),
(3, 'RH-A004', 4,  'free',    5, 4),
(3, 'RH-A005', 5,  'free',    5, 4),
(3, 'RH-A006', 6,  'using',   5, 4),
(3, 'RH-A007', 7,  'free',    5, 4),
(3, 'RH-A008', 8,  'free',    6, 4),
(3, 'RH-A009', 9,  'free',    6, 4),
(3, 'RH-A010', 10, 'using',   6, 4),
(3, 'RH-A011', 11, 'free',    7, 4),
(3, 'RH-A012', 12, 'free',    7, 4),
-- 万象城（柜群5）：10台
(3, 'RH-B001', 1,  'free',    4, 5),
(3, 'RH-B002', 2,  'free',    4, 5),
(3, 'RH-B003', 3,  'free',    5, 5),
(3, 'RH-B004', 4,  'free',    5, 5),
(3, 'RH-B005', 5,  'using',   5, 5),
(3, 'RH-B006', 6,  'free',    6, 5),
(3, 'RH-B007', 7,  'free',    6, 5),
(3, 'RH-B008', 8,  'free',    7, 5),
(3, 'RH-B009', 9,  'free',    8, 5),
(3, 'RH-B010', 10, 'free',    8, 5),
-- 世界之窗（柜群6）：8台
(3, 'RH-C001', 1, 'free',     4, 6),
(3, 'RH-C002', 2, 'free',     5, 6),
(3, 'RH-C003', 3, 'free',     5, 6),
(3, 'RH-C004', 4, 'free',     6, 6),
(3, 'RH-C005', 5, 'free',     6, 6),
(3, 'RH-C006', 6, 'free',     7, 6),
(3, 'RH-C007', 7, 'using',    7, 6),
(3, 'RH-C008', 8, 'free',     7, 6),
-- 宝安机场T3（柜群7，已禁用）：6台
(3, 'RH-D001', 1, 'forbidden', 5, 7),
(3, 'RH-D002', 2, 'forbidden', 5, 7),
(3, 'RH-D003', 3, 'forbidden', 6, 7),
(3, 'RH-D004', 4, 'forbidden', 6, 7),
(3, 'RH-D005', 5, 'forbidden', 7, 7),
(3, 'RH-D006', 6, 'forbidden', 7, 7),
-- 厂商3 未分配：8台
(3, 'RH-X001', NULL, 'free', NULL, NULL),
(3, 'RH-X002', NULL, 'free', NULL, NULL),
(3, 'RH-X003', NULL, 'free', NULL, NULL),
(3, 'RH-X004', NULL, 'free', NULL, NULL),
(3, 'RH-X005', NULL, 'free', NULL, NULL),
(3, 'RH-X006', NULL, 'free', NULL, NULL),
(3, 'RH-X007', NULL, 'free', NULL, NULL),
(3, 'RH-X008', NULL, 'free', NULL, NULL);

