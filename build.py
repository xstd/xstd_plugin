#!/usr/bin/python
#-*- coding: utf-8 -*-

import os
import sys
import re
import optparse
import myLib

CHECK_FILE = ['AndroidManifest.xml', 'res', 'src', 'src_lib']
FILE_SUBFIX = ['.java', '.xml']
MENIFEST_FILE = 'AndroidManifest.xml'

STRING_FILE = 'res/values/strings.xml'
BUILD_RES_DIR = 'build_res/'
ASSETS_DIR = 'assets/'
ICON_RES_PATH = 'res/drawable-xhdpi/'

init_optprarse = optparse.OptionParser(usage='python build.py [-f your_build_config_file] [-p package name] [-n app name] [-t target_save]')
init_optprarse.add_option('-p', '--package', dest='package')
init_optprarse.add_option('-n', '--name', dest='name')
init_optprarse.add_option('-t', '--target', dest='target')
init_optprarse.add_option('-f', '--file', dest='file')

class ARGUMENTS_ERROR(Exception):
    """ replace text failure
    """

class RES_ERROR(Exception):
    """ build resource error
    """

#根据Menifest来获取现在的packageName
def __getPackageName():
    if os.path.exists(MENIFEST_FILE):
        with open(MENIFEST_FILE, 'r') as mfile:
            for line in mfile:
                m = re.search('package=\".*\"', line)
                if m:
                    oldStr = m.group(0)
                    #print oldStr + ' left index = ' + str(oldStr.find('\"')) + ' right index = ' + str(oldStr.rfind('\"'))
                    return oldStr[oldStr.find('\"') + 1:oldStr.rfind('\"')]

    return None

#更具Menifest获取当前的VersionName
def __getVersionName():
    if os.path.exists(MENIFEST_FILE):
        with open(MENIFEST_FILE, 'r') as file:
            for line in file:
                m = re.search('android:versionName=\".*\"', line)
                if m:
                    oldStr = m.group(0)
                    return oldStr[oldStr.find('\"') + 1:oldStr.rfind('\"')]
    return None
#替换filename中的文案。如果filename是文件，直接替换，如果filename是文件夹
#递归替换filename文件夹下的所有文件
def __walk_replace_file(filename, old, new):
    if filename == None or len(filename) == 0:
        raise ARGUMENTS_ERROR()

    if os.path.isfile(filename):
        if __check_file_extend(filename):
            print 'find one file can replace, file : %s' % filename
            if filename != 'Config.java':
                myLib.replce_text_in_file(filename, old, new)
    elif os.path.isdir(filename):
        wpath = os.walk(filename)
        for item in wpath:
            files = item[2]
            parentPath = item[0]
            for f in files:
                if __check_file_extend(f):
                    #注意，Config文件比较特殊，不做替换
                    if f != 'Config.java':
                        print 'find one file can replace, file : %s/%s' % (parentPath, f)
                        myLib.replce_text_in_file('%s/%s' % (parentPath, f), old, new)
    
    return True
                
#检查当前文件是否是.java 和 .xml文件
def __check_file_extend(filename):
    for end in FILE_SUBFIX:
        if filename.endswith(end):
            return True
    return False

def __replace_package_name(new_package_name):
    if new_package_name == None or len(new_package_name) == 0:
        raise ARGUMENTS_ERROR()

    old_package = __getPackageName()

    print '[[replace.py]] try to replace old package : %s to new pacakge : %s' % (old_package, new_package_name)
    for item in CHECK_FILE:
        __walk_replace_file(item, old_package, new_package_name)

    return True

def __main(args):
    opt, arg = init_optprarse.parse_args(args)
    new_package = opt.package
    name = opt.name
    target = opt.target

    if new_package == None:
        raise ARGUMENTS_ERROR()

    __replace_package_name(new_package)

    if name != None and len(name) > 0:
        myLib.replce_text_in_file(STRING_FILE, 'app_name.*>', 'app_name">%s</string>' % name)

    print '='*20 + ' build prepare finish ' + '='*20
    print 'begin build now'
    os.system('ant clean ; ant release')

    if os.path.exists('bin/XSTD_plugin-release.apk') and target != None:
        if not os.path.exists(target):
            os.mkdirs(target)

        version_name = __getVersionName()
        target_apk_file = '%s_%s_%s.apk' % (new_package, version_name, name)
        os.system('cp -rf bin/XSTD_plugin-release.apk %s/%s' % (target, target_apk_file))

        print 'backup the build target %s/%s success >>>>>>>>' % (target, target_apk_file)

    print 'after build for new package : %s, just reset code ' % new_package
    #os.system('git reset --hard HEAD')

if __name__ == '__main__':
    __main(sys.argv[1:])
