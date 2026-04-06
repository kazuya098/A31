import service from '../api';

/**
 * 后端框架连通性测试接口
 * 用于确认后端已启动、可访问
 */
export function getHealth() {
  return service({
    url: '/health',
    method: 'get'
  });
}
