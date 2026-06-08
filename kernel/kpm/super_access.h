#ifndef __SUKISU_SUPER_ACCESS_H
#define __SUKISU_SUPER_ACCESS_H

#include <linux/types.h>
#include <linux/stddef.h>
#include "kpm.h"
#include "compact.h"

extern int mizusu_super_find_struct(const char *struct_name, size_t *out_size,
                                    int *out_members);
extern int mizusu_super_access(const char *struct_name, const char *member_name,
                               size_t *out_offset, size_t *out_size);
extern int mizusu_super_container_of(const char *struct_name,
                                     const char *member_name, void *ptr,
                                     void **out_ptr);

#endif