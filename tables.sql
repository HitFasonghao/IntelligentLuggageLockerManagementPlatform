create database locker_management_platform;

-- 平台管理员表
CREATE TABLE platform_admins (
                                 admin_id INT NOT NULL AUTO_INCREMENT COMMENT '管理员标识',
                                 username VARCHAR(30) NOT NULL COMMENT '用户名',
                                 password VARCHAR(20) NOT NULL COMMENT '密码',
                                 real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
                                 email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                                 phone VARCHAR(11) NOT NULL COMMENT '电话',
                                 avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                                 is_super_admin BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为超级管理员',
                                 is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否被启用',
                                 last_login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次登录时间',
                                 created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近修改时间',
                                 PRIMARY KEY (admin_id),
                                 UNIQUE KEY uk_username (username),
                                 UNIQUE KEY uk_email (email),
                                 UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台管理员表';

-- 厂商用户表
CREATE TABLE vendor_users (
                              vendor_user_id INT NOT NULL AUTO_INCREMENT COMMENT '厂商用户标识',
                              username VARCHAR(30) NOT NULL COMMENT '用户名',
                              password VARCHAR(20) DEFAULT NULL COMMENT '密码',
                              email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                              phone VARCHAR(11) NOT NULL COMMENT '电话',
                              real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
                              avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                              status ENUM('active','locked','inactive') NOT NULL DEFAULT 'active' COMMENT '账号状态：active-启用，locked-锁定，inactive-未启用',
                              failed_login_number INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
                              last_login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后一次登录时间',
                              password_changed_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后一次密码修改时间',
                              created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
                              PRIMARY KEY (vendor_user_id),
                              UNIQUE KEY uk_username (username),
                              UNIQUE KEY uk_email (email),
                              UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='厂商用户表';

-- PC端页面权限（菜单按钮）表
CREATE TABLE pc_permission (
                               permission_id INT NOT NULL AUTO_INCREMENT COMMENT '权限标识',
                               name VARCHAR(50) NOT NULL COMMENT '名称',
                               code VARCHAR(100) NOT NULL COMMENT '编码',
                               type ENUM('MENU','BUTTON') NOT NULL COMMENT '类型',
                               parent_id INT NULL DEFAULT NULL COMMENT '父级标识',
                               path VARCHAR(255) NULL DEFAULT NULL COMMENT '路由路径',
                               redirect VARCHAR(255) NULL DEFAULT NULL COMMENT '路由重定向地址',
                               icon VARCHAR(100) NULL DEFAULT NULL COMMENT '菜单图标',
                               component VARCHAR(255) NULL DEFAULT NULL COMMENT '前端组件路径',
                               layout VARCHAR(50) NULL DEFAULT NULL COMMENT '页面布局类型',
                               keep_alive BOOLEAN NULL DEFAULT NULL COMMENT '是否缓存页面',
                               method VARCHAR(20) NULL DEFAULT NULL COMMENT '预留字段',
                               description VARCHAR(255) NULL DEFAULT NULL COMMENT '描述',
                               show_status BOOLEAN NOT NULL COMMENT '是否显示',
                               enable_status BOOLEAN NOT NULL COMMENT '是否启用',
                               sort INT NOT NULL COMMENT '排序号',
                               created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
                               PRIMARY KEY (permission_id),
                               UNIQUE KEY uk_permission_code (code),
                               KEY idx_permission_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PC端页面权限（菜单按钮）表';

-- PC端角色表
CREATE TABLE pc_role (
                         role_id INT NOT NULL AUTO_INCREMENT COMMENT '角色标识',
                         name VARCHAR(50) NOT NULL COMMENT '名称',
                         code VARCHAR(50) NOT NULL COMMENT '编码',
                         description VARCHAR(255) NULL DEFAULT NULL COMMENT '描述',
                         created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
                         PRIMARY KEY (role_id),
                         UNIQUE KEY uk_role_name (name),
                         UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PC端角色表';

-- PC端角色权限关联表
CREATE TABLE pc_role_permission (
                                    role_permission_id INT NOT NULL AUTO_INCREMENT COMMENT '关联标识',
                                    role_id INT NOT NULL COMMENT '角色标识',
                                    permission_id INT NOT NULL COMMENT '权限标识',
                                    created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    PRIMARY KEY (role_permission_id),
                                    UNIQUE KEY uk_role_permission (role_id, permission_id),
                                    KEY idx_role_permission_permission_id (permission_id),
                                    CONSTRAINT fk_role_permission_role_id FOREIGN KEY (role_id) REFERENCES pc_role (role_id) ON DELETE CASCADE ON UPDATE CASCADE,
                                    CONSTRAINT fk_role_permission_permission_id FOREIGN KEY (permission_id) REFERENCES pc_permission (permission_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PC端角色权限关联表';

-- 厂商表
CREATE TABLE vendors (
                         vendor_id INT NOT NULL AUTO_INCREMENT COMMENT '厂商标识',
                         company_name VARCHAR(100) NOT NULL COMMENT '公司全称',
                         short_name VARCHAR(50) DEFAULT NULL COMMENT '简称',
                         license_no VARCHAR(50) DEFAULT NULL COMMENT '营业执照号',
                         license_image VARCHAR(500) DEFAULT NULL COMMENT '营业执照照片(URL地址)',
                         legal_person VARCHAR(50) DEFAULT NULL COMMENT '法定代表人',
                         legal_person_id VARCHAR(30) DEFAULT NULL COMMENT '法人身份证号',
                         contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
                         contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
                         contact_email VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
                         company_address VARCHAR(300) DEFAULT NULL COMMENT '公司地址',
                         website VARCHAR(200) DEFAULT NULL COMMENT '官网',
                         introduction TEXT DEFAULT NULL COMMENT '公司介绍',
                         business_scope TEXT DEFAULT NULL COMMENT '经营范围',
                         api_endpoint VARCHAR(500) DEFAULT NULL COMMENT 'API接口地址',
                         vendor_access_token VARCHAR(500) DEFAULT NULL COMMENT '厂商系统访问Token',
                         platform_access_token VARCHAR(500) DEFAULT NULL COMMENT '平台访问Token',
                         status ENUM('draft','pending','testing','approved','rejected','suspended','banned') NOT NULL DEFAULT 'draft' COMMENT '状态',
                         submitted_time TIMESTAMP NULL DEFAULT NULL COMMENT '提交审核的时间',
                         reviewed_time TIMESTAMP NULL DEFAULT NULL COMMENT '平台完成资质审核时间',
                         approved_time TIMESTAMP NULL DEFAULT NULL COMMENT '最终审核批准时间',
                         effective_from TIMESTAMP NULL DEFAULT NULL COMMENT '生效日期',
                         effective_to TIMESTAMP NULL DEFAULT NULL COMMENT '失效日期',
                         admin_id INT DEFAULT NULL COMMENT '审批人标识（平台管理员）',
                         created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
                         PRIMARY KEY (vendor_id),
                         UNIQUE KEY uk_company_name (company_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厂商表';

-- 厂商用户与厂商关联表
CREATE TABLE vendor_user_relation (
                                      vendor_user_relation_id INT NOT NULL AUTO_INCREMENT COMMENT '关联记录标识',
                                      vendor_user_id INT NOT NULL COMMENT '厂商用户标识',
                                      vendor_id INT NOT NULL COMMENT '厂商标识',
                                      is_main BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为主管理员',
                                      PRIMARY KEY (vendor_user_relation_id),
                                      KEY idx_vendor_user_id (vendor_user_id),
                                      KEY idx_vendor_id (vendor_id),
                                      CONSTRAINT fk_vur_vendor_id FOREIGN KEY (vendor_id) REFERENCES vendors (vendor_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厂商用户与厂商关联表';

-- 审核记录表
CREATE TABLE vendor_audit_records (
                                      audit_record_id INT NOT NULL AUTO_INCREMENT COMMENT '审核记录标识',
                                      vendor_id INT NOT NULL COMMENT '厂商标识',
                                      vendor_user_id INT DEFAULT NULL COMMENT '申请用户标识（厂商用户）',
                                      round INT NOT NULL COMMENT '审核轮次',
                                      type ENUM('initial','renewal','change','complaint') NOT NULL COMMENT '审核类型',
                                      data JSON NOT NULL COMMENT '提交数据快照',
                                      admin_id INT NOT NULL COMMENT '审核员（平台管理员）标识',
                                      created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      completed_time TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
                                      result VARCHAR(50) DEFAULT 'not_started' COMMENT '审核进度结果',
                                      PRIMARY KEY (audit_record_id),
                                      KEY idx_vendor_id (vendor_id),
                                      CONSTRAINT fk_var_vendor_id FOREIGN KEY (vendor_id) REFERENCES vendors (vendor_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 审核流程节点表
CREATE TABLE audit_nodes (
                             audit_node_id INT NOT NULL AUTO_INCREMENT COMMENT '节点标识',
                             name VARCHAR(50) NOT NULL COMMENT '节点名称',
                             type ENUM('qualification','functional_test','performance','compliance','manual_review') NOT NULL COMMENT '节点类型',
                             `order` INT NOT NULL COMMENT '顺序',
                             auto_pass BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否自动通过',
                             timeout_hours INT NOT NULL DEFAULT 24 COMMENT '超时时间（小时）',
                             is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
                             PRIMARY KEY (audit_node_id),
                             UNIQUE KEY uk_node_order (type, `order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核流程节点表';

-- 审核任务表
CREATE TABLE audit_tasks (
                             audit_task_id INT NOT NULL AUTO_INCREMENT COMMENT '任务标识',
                             vendor_id INT NOT NULL DEFAULT 0 COMMENT '厂商标识',
                             audit_record_id INT NOT NULL DEFAULT 0 COMMENT '审核记录标识',
                             audit_node_id INT NOT NULL COMMENT '审核节点标识',
                             admin_id INT DEFAULT NULL COMMENT '审核员标识',
                             status ENUM('pending','in_progress','completed','overdue') NOT NULL DEFAULT 'pending' COMMENT '状态',
                             priority ENUM('low','medium','high','urgent') DEFAULT NULL COMMENT '优先级',
                             due_date TIMESTAMP NULL DEFAULT NULL COMMENT '截止时间',
                             completed_time TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
                             notes TEXT DEFAULT NULL COMMENT '审核意见',
                             passed BOOLEAN DEFAULT NULL COMMENT '是否通过',
                             created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (audit_task_id),
                             KEY idx_vendor_id (vendor_id),
                             KEY idx_audit_record_id (audit_record_id),
                             KEY idx_audit_node_id (audit_node_id),
                             KEY idx_admin_id (admin_id),
                             CONSTRAINT fk_at_audit_node_id FOREIGN KEY (audit_node_id) REFERENCES audit_nodes (audit_node_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核任务表';


-- 寄存柜种类表
CREATE TABLE cabinet_kinds (
                               kind_id INT NOT NULL AUTO_INCREMENT COMMENT '寄存柜种类标识',
                               vendor_id INT NOT NULL COMMENT '厂商标识',
                               name VARCHAR(60) NOT NULL COMMENT '种类名称',
                               description TEXT COMMENT '描述',
                               charge DECIMAL(10,2) NOT NULL COMMENT '收费金额(单位/元)',
                               time_unit ENUM('anHour','halfAnHour','tenMinutes') NOT NULL COMMENT '收费时间单位',
                               PRIMARY KEY (kind_id),
                               CONSTRAINT fk_cabinets_kinds_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (vendor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寄存柜种类表';

-- 柜群表
CREATE TABLE clusters (
                          cluster_id INT NOT NULL AUTO_INCREMENT COMMENT '柜群标识',
                          vendor_id INT NOT NULL COMMENT '厂商标识',
                          name VARCHAR(60) NOT NULL COMMENT '柜群名称',
                          location VARCHAR(300) NOT NULL COMMENT '柜群地址',
                          longitude DECIMAL(11,6) COMMENT '经度',
                          dimension DECIMAL(10,6) COMMENT '纬度',
                          status ENUM('using','forbidden') NOT NULL COMMENT '状态',
                          description TEXT COMMENT '描述',
                          created_time TIMESTAMP NOT NULL COMMENT '创建时间',
                          updated_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
                          PRIMARY KEY (cluster_id),
                          CONSTRAINT fk_clusters_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (vendor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='柜群表';

-- 寄存柜表
CREATE TABLE cabinets (
                          cabinet_id INT NOT NULL AUTO_INCREMENT COMMENT '寄存柜标识',
                          vendor_id INT NOT NULL COMMENT '厂商标识',
                          device_id VARCHAR(30) NOT NULL COMMENT '设备id',
                          number INT COMMENT '格口号',
                          status ENUM('free','opening','using','forbidden') NOT NULL COMMENT '状态',
                          kind_id INT COMMENT '寄存柜种类标识',
                          cluster_id INT COMMENT '柜群标识',
                          PRIMARY KEY (cabinet_id),
                          CONSTRAINT fk_cabinets_vendor FOREIGN KEY (vendor_id) REFERENCES vendors (vendor_id),
                          CONSTRAINT fk_cabinets_kind FOREIGN KEY (kind_id) REFERENCES cabinet_kinds (kind_id),
                          CONSTRAINT fk_cabinets_cluster FOREIGN KEY (cluster_id) REFERENCES clusters (cluster_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寄存柜表';
