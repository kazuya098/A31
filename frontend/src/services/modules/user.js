import service from '../api';

export function getUserInfo() {
  return service({
    url: '/user/info',
    method: 'get',
  });
}

export function updateUserInfo(data) {
  return service({
    url: '/user/update',
    method: 'put',
    data,
  });
}

export function restoreData() {
  return service({
    url: '/user/restore-data',
    method: 'post',
  });
}
