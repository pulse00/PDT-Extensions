PDT-Extensions migration
========================

## Migration

The funcionality of this plugin has been migrated to the [PDT Extension group].

The old updatesite will still host this plugin until the PDT Extension Group Core Plugin is 
released in a stable version. 

If you want to get the latest and greatest additions/bugfixes, please migrate your eclipse installation
to use the PDT Extension Group Core Plugin (see http://p2-dev.pdt-extensions.org/ ).

To migrate your existing eclipse installation from this repository to the (beta) PDT Extension Core Plugin, you 
need to:

1. Uninstall PDT-Extensions plugin from your existing eclipse instance.
2. Restart eclipse
3. Add http://p2-dev.pdt-extensions.org/ to your updatesites
4. Install __PDT Extensions 0.13.xxx__ from the pdt-extensions.org updatesite


## Formatter settings

If you had custom formatter settings, they can be migrated easily to the PDT Extension [Core Plugin](https://github.com/pdt-eg/Core-Plugin)
by going through the following steps:

1. Export your existing formatter settings (PHP -> Code Style -> Formatter (PDT Tool) -> Export All) prior to uninstalling the old plugin
2. Migrate your installation as described above
3. Import the formatter settings into the new plugin
