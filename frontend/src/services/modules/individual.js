import service from '../api';

export function getIndividuals(params) {
  return service({
    url: '/individual/list',
    method: 'get',
    params,
  });
}

export function addIndividual(data) {
  return service({
    url: '/individual/add',
    method: 'post',
    data,
  });
}

export function updateIndividual(data) {
  return service({
    url: '/individual/update',
    method: 'put',
    data,
  });
}

export function deleteIndividual(id) {
  return service({
    url: `/individual/${id}`,
    method: 'delete',
  });
}
