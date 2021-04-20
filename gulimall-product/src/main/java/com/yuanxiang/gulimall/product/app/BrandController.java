package com.yuanxiang.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.yuanxiang.common.valid.AddGroup;
import com.yuanxiang.common.valid.UpdateGroup;
import com.yuanxiang.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.yuanxiang.gulimall.product.entity.BrandEntity;
import com.yuanxiang.gulimall.product.service.BrandService;
import com.yuanxiang.common.utils.PageUtils;
import com.yuanxiang.common.utils.R;


/**
 * 品牌
 *
 * @author yuanxiang
 * @email sunlightcs@gmail.com
 * @date 2021-03-16 12:06:37
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 获得所有品牌Id
     */
    @GetMapping("/infos")
    public R getBrandInfos(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> entities = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brand", entities);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
        /*if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach((item) -> {
                //获得错误提示
                String message = item.getDefaultMessage();
                //获得错误属性的名字
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(400, "提交的数据不合法").put("data",map);
        } else {
        }*/

        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateGroup.class})@RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated({UpdateStatusGroup.class})@RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
