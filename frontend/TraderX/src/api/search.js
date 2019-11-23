import request from '@/utils/request'

export function searchUser(userName) {
  return request({
    url: '/users/profile/' + userName,
    method: 'get'
  })
}

export function getAllUsers() {
  return request({
    url: '/users/getAll',
    method: 'get'
  })
}
